package com.gitserver.service;

import com.gitserver.dto.*;
import com.gitserver.entity.GitRepository;
import com.gitserver.entity.Issue;
import com.gitserver.entity.Issue.IssueState;
import com.gitserver.exception.IssueNotFoundException;
import com.gitserver.exception.RepositoryNotFoundException;
import com.gitserver.repository.GitRepositoryJpaRepository;
import com.gitserver.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for issue management operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueRepository issueRepository;
    private final GitRepositoryJpaRepository repositoryJpaRepository;

    /**
     * Create a new issue.
     */
    @Transactional
    public IssueResponse createIssue(String owner, String repoName, String authorUsername, CreateIssueRequest request) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, repoName)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, repoName));

        Integer nextIssueNumber = issueRepository.findMaxIssueNumberByRepositoryId(repository.getId())
                .map(n -> n + 1)
                .orElse(1);

        Issue issue = Issue.builder()
                .issueNumber(nextIssueNumber)
                .title(request.getTitle())
                .body(request.getBody())
                .state(IssueState.OPEN)
                .repositoryId(repository.getId())
                .authorUsername(authorUsername)
                .assigneeUsername(request.getAssignee())
                .labels(request.getLabels())
                .build();

        issue = issueRepository.save(issue);
        log.info("Created issue #{} in repository {}/{}", nextIssueNumber, owner, repoName);

        return toResponse(issue, owner, repoName);
    }

    /**
     * Get an issue by number.
     */
    public IssueResponse getIssue(String owner, String repoName, Integer issueNumber) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, repoName)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, repoName));

        Issue issue = issueRepository.findByRepositoryIdAndIssueNumber(repository.getId(), issueNumber)
                .orElseThrow(() -> new IssueNotFoundException(owner, repoName, issueNumber));

        return toResponse(issue, owner, repoName);
    }

    /**
     * Get all issues for a repository.
     */
    public List<IssueResponse> getIssues(String owner, String repoName, String state) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, repoName)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, repoName));

        List<Issue> issues;
        if (state != null && !state.isEmpty()) {
            IssueState issueState = IssueState.valueOf(state.toUpperCase());
            issues = issueRepository.findByRepositoryIdAndStateOrderByCreatedAtDesc(repository.getId(), issueState);
        } else {
            issues = issueRepository.findByRepositoryIdOrderByCreatedAtDesc(repository.getId());
        }

        return issues.stream()
                .map(issue -> toResponse(issue, owner, repoName))
                .collect(Collectors.toList());
    }

    /**
     * Update an issue.
     */
    @Transactional
    public IssueResponse updateIssue(String owner, String repoName, Integer issueNumber, UpdateIssueRequest request) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, repoName)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, repoName));

        Issue issue = issueRepository.findByRepositoryIdAndIssueNumber(repository.getId(), issueNumber)
                .orElseThrow(() -> new IssueNotFoundException(owner, repoName, issueNumber));

        if (request.getTitle() != null) {
            issue.setTitle(request.getTitle());
        }
        if (request.getBody() != null) {
            issue.setBody(request.getBody());
        }
        if (request.getState() != null) {
            IssueState newState = IssueState.valueOf(request.getState().toUpperCase());
            if (newState == IssueState.CLOSED && issue.getState() != IssueState.CLOSED) {
                issue.setClosedAt(LocalDateTime.now());
            }
            issue.setState(newState);
        }
        if (request.getAssignee() != null) {
            issue.setAssigneeUsername(request.getAssignee());
        }
        if (request.getLabels() != null) {
            issue.setLabels(request.getLabels());
        }

        issue = issueRepository.save(issue);
        log.info("Updated issue #{} in repository {}/{}", issueNumber, owner, repoName);

        return toResponse(issue, owner, repoName);
    }

    /**
     * Close an issue.
     */
    @Transactional
    public IssueResponse closeIssue(String owner, String repoName, Integer issueNumber) {
        UpdateIssueRequest request = UpdateIssueRequest.builder().state("CLOSED").build();
        return updateIssue(owner, repoName, issueNumber, request);
    }

    /**
     * Reopen an issue.
     */
    @Transactional
    public IssueResponse reopenIssue(String owner, String repoName, Integer issueNumber) {
        UpdateIssueRequest request = UpdateIssueRequest.builder().state("OPEN").build();
        return updateIssue(owner, repoName, issueNumber, request);
    }

    /**
     * Count open issues for a repository.
     */
    public long countOpenIssues(Long repositoryId) {
        return issueRepository.countByRepositoryIdAndState(repositoryId, IssueState.OPEN);
    }

    /**
     * Count closed issues for a repository.
     */
    public long countClosedIssues(Long repositoryId) {
        return issueRepository.countByRepositoryIdAndState(repositoryId, IssueState.CLOSED);
    }

    private IssueResponse toResponse(Issue issue, String owner, String repoName) {
        return IssueResponse.builder()
                .id(issue.getId())
                .issueNumber(issue.getIssueNumber())
                .title(issue.getTitle())
                .body(issue.getBody())
                .state(issue.getState().name())
                .author(issue.getAuthorUsername())
                .assignee(issue.getAssigneeUsername())
                .labels(issue.getLabels())
                .repositoryOwner(owner)
                .repositoryName(repoName)
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .closedAt(issue.getClosedAt())
                .build();
    }
}
