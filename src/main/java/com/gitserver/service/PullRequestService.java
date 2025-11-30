package com.gitserver.service;

import com.gitserver.dto.*;
import com.gitserver.entity.GitRepository;
import com.gitserver.entity.PullRequest;
import com.gitserver.entity.PullRequest.PullRequestState;
import com.gitserver.exception.PullRequestNotFoundException;
import com.gitserver.exception.RepositoryNotFoundException;
import com.gitserver.exception.BranchNotFoundException;
import com.gitserver.git.JGitService;
import com.gitserver.repository.GitRepositoryJpaRepository;
import com.gitserver.repository.PullRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for pull request management operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PullRequestService {

    private final PullRequestRepository pullRequestRepository;
    private final GitRepositoryJpaRepository repositoryJpaRepository;
    private final JGitService jGitService;

    /**
     * Create a new pull request.
     */
    @Transactional
    public PullRequestResponse createPullRequest(String owner, String repoName, String authorUsername, CreatePullRequestRequest request) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, repoName)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, repoName));

        // Verify branches exist
        List<BranchInfo> branches = jGitService.getBranches(owner, repoName);
        boolean headExists = branches.stream().anyMatch(b -> b.getName().equals(request.getHeadBranch()));
        boolean baseExists = branches.stream().anyMatch(b -> b.getName().equals(request.getBaseBranch()));

        if (!headExists) {
            throw new BranchNotFoundException(owner + "/" + repoName, request.getHeadBranch());
        }
        if (!baseExists) {
            throw new BranchNotFoundException(owner + "/" + repoName, request.getBaseBranch());
        }

        Integer nextPrNumber = pullRequestRepository.findMaxPrNumberByRepositoryId(repository.getId())
                .map(n -> n + 1)
                .orElse(1);

        PullRequest pullRequest = PullRequest.builder()
                .prNumber(nextPrNumber)
                .title(request.getTitle())
                .body(request.getBody())
                .state(PullRequestState.OPEN)
                .repositoryId(repository.getId())
                .authorUsername(authorUsername)
                .headBranch(request.getHeadBranch())
                .baseBranch(request.getBaseBranch())
                .labels(request.getLabels())
                .reviewers(request.getReviewers())
                .isMerged(false)
                .build();

        pullRequest = pullRequestRepository.save(pullRequest);
        log.info("Created pull request #{} in repository {}/{}", nextPrNumber, owner, repoName);

        return toResponse(pullRequest, owner, repoName);
    }

    /**
     * Get a pull request by number.
     */
    public PullRequestResponse getPullRequest(String owner, String repoName, Integer prNumber) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, repoName)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, repoName));

        PullRequest pullRequest = pullRequestRepository.findByRepositoryIdAndPrNumber(repository.getId(), prNumber)
                .orElseThrow(() -> new PullRequestNotFoundException(owner, repoName, prNumber));

        return toResponse(pullRequest, owner, repoName);
    }

    /**
     * Get all pull requests for a repository.
     */
    public List<PullRequestResponse> getPullRequests(String owner, String repoName, String state) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, repoName)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, repoName));

        List<PullRequest> pullRequests;
        if (state != null && !state.isEmpty()) {
            PullRequestState prState = PullRequestState.valueOf(state.toUpperCase());
            pullRequests = pullRequestRepository.findByRepositoryIdAndStateOrderByCreatedAtDesc(repository.getId(), prState);
        } else {
            pullRequests = pullRequestRepository.findByRepositoryIdOrderByCreatedAtDesc(repository.getId());
        }

        return pullRequests.stream()
                .map(pr -> toResponse(pr, owner, repoName))
                .collect(Collectors.toList());
    }

    /**
     * Update a pull request.
     */
    @Transactional
    public PullRequestResponse updatePullRequest(String owner, String repoName, Integer prNumber, UpdatePullRequestRequest request) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, repoName)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, repoName));

        PullRequest pullRequest = pullRequestRepository.findByRepositoryIdAndPrNumber(repository.getId(), prNumber)
                .orElseThrow(() -> new PullRequestNotFoundException(owner, repoName, prNumber));

        if (request.getTitle() != null) {
            pullRequest.setTitle(request.getTitle());
        }
        if (request.getBody() != null) {
            pullRequest.setBody(request.getBody());
        }
        if (request.getState() != null) {
            PullRequestState newState = PullRequestState.valueOf(request.getState().toUpperCase());
            if (newState == PullRequestState.CLOSED && pullRequest.getState() != PullRequestState.CLOSED) {
                pullRequest.setClosedAt(LocalDateTime.now());
            }
            pullRequest.setState(newState);
        }
        if (request.getLabels() != null) {
            pullRequest.setLabels(request.getLabels());
        }
        if (request.getReviewers() != null) {
            pullRequest.setReviewers(request.getReviewers());
        }

        pullRequest = pullRequestRepository.save(pullRequest);
        log.info("Updated pull request #{} in repository {}/{}", prNumber, owner, repoName);

        return toResponse(pullRequest, owner, repoName);
    }

    /**
     * Merge a pull request.
     */
    @Transactional
    public PullRequestResponse mergePullRequest(String owner, String repoName, Integer prNumber, String mergedBy) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, repoName)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, repoName));

        PullRequest pullRequest = pullRequestRepository.findByRepositoryIdAndPrNumber(repository.getId(), prNumber)
                .orElseThrow(() -> new PullRequestNotFoundException(owner, repoName, prNumber));

        if (pullRequest.isMerged()) {
            throw new IllegalStateException("Pull request is already merged");
        }

        if (pullRequest.getState() != PullRequestState.OPEN) {
            throw new IllegalStateException("Can only merge open pull requests");
        }

        // Perform Git merge
        jGitService.mergeBranch(owner, repoName, pullRequest.getHeadBranch(), pullRequest.getBaseBranch(), mergedBy);

        pullRequest.setMerged(true);
        pullRequest.setMergedBy(mergedBy);
        pullRequest.setMergedAt(LocalDateTime.now());
        pullRequest.setState(PullRequestState.MERGED);
        pullRequest.setClosedAt(LocalDateTime.now());

        pullRequest = pullRequestRepository.save(pullRequest);
        log.info("Merged pull request #{} in repository {}/{}", prNumber, owner, repoName);

        return toResponse(pullRequest, owner, repoName);
    }

    /**
     * Close a pull request.
     */
    @Transactional
    public PullRequestResponse closePullRequest(String owner, String repoName, Integer prNumber) {
        UpdatePullRequestRequest request = UpdatePullRequestRequest.builder().state("CLOSED").build();
        return updatePullRequest(owner, repoName, prNumber, request);
    }

    /**
     * Get diff for a pull request.
     */
    public String getPullRequestDiff(String owner, String repoName, Integer prNumber) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, repoName)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, repoName));

        PullRequest pullRequest = pullRequestRepository.findByRepositoryIdAndPrNumber(repository.getId(), prNumber)
                .orElseThrow(() -> new PullRequestNotFoundException(owner, repoName, prNumber));

        return jGitService.getDiff(owner, repoName, pullRequest.getBaseBranch(), pullRequest.getHeadBranch());
    }

    /**
     * Count open pull requests for a repository.
     */
    public long countOpenPullRequests(Long repositoryId) {
        return pullRequestRepository.countByRepositoryIdAndState(repositoryId, PullRequestState.OPEN);
    }

    /**
     * Count closed pull requests for a repository.
     */
    public long countClosedPullRequests(Long repositoryId) {
        return pullRequestRepository.countByRepositoryIdAndState(repositoryId, PullRequestState.CLOSED);
    }

    /**
     * Count merged pull requests for a repository.
     */
    public long countMergedPullRequests(Long repositoryId) {
        return pullRequestRepository.countByRepositoryIdAndState(repositoryId, PullRequestState.MERGED);
    }

    private PullRequestResponse toResponse(PullRequest pullRequest, String owner, String repoName) {
        return PullRequestResponse.builder()
                .id(pullRequest.getId())
                .prNumber(pullRequest.getPrNumber())
                .title(pullRequest.getTitle())
                .body(pullRequest.getBody())
                .state(pullRequest.getState().name())
                .author(pullRequest.getAuthorUsername())
                .headBranch(pullRequest.getHeadBranch())
                .baseBranch(pullRequest.getBaseBranch())
                .labels(pullRequest.getLabels())
                .reviewers(pullRequest.getReviewers())
                .isMerged(pullRequest.isMerged())
                .mergedBy(pullRequest.getMergedBy())
                .mergedAt(pullRequest.getMergedAt())
                .repositoryOwner(owner)
                .repositoryName(repoName)
                .createdAt(pullRequest.getCreatedAt())
                .updatedAt(pullRequest.getUpdatedAt())
                .closedAt(pullRequest.getClosedAt())
                .build();
    }
}
