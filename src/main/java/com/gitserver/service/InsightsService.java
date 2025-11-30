package com.gitserver.service;

import com.gitserver.dto.CommitInfo;
import com.gitserver.dto.RepositoryInsights;
import com.gitserver.entity.GitRepository;
import com.gitserver.exception.RepositoryNotFoundException;
import com.gitserver.git.JGitService;
import com.gitserver.repository.GitRepositoryJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for repository insights/statistics.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InsightsService {

    private final GitRepositoryJpaRepository repositoryJpaRepository;
    private final JGitService jGitService;
    private final IssueService issueService;
    private final PullRequestService pullRequestService;

    /**
     * Get comprehensive insights for a repository.
     */
    public RepositoryInsights getRepositoryInsights(String owner, String repoName) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, repoName)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, repoName));

        String defaultBranch = repository.getDefaultBranch();

        // Get commits (up to 1000 for statistics)
        List<CommitInfo> commits = jGitService.getCommits(owner, repoName, defaultBranch, 1000);
        
        // Calculate commit statistics
        long oneWeekAgoMs = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
        long oneMonthAgoMs = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L);
        
        int commitsLastWeek = (int) commits.stream()
                .filter(c -> c.getAuthorTime() > oneWeekAgoMs)
                .count();
        
        int commitsLastMonth = (int) commits.stream()
                .filter(c -> c.getAuthorTime() > oneMonthAgoMs)
                .count();

        // Count unique contributors
        Set<String> contributors = new HashSet<>();
        for (CommitInfo commit : commits) {
            contributors.add(commit.getAuthor());
        }

        // Get branch count
        int totalBranches = jGitService.getBranches(owner, repoName).size();

        // Get file count from root directory recursively
        int totalFiles = countFiles(owner, repoName, defaultBranch, "");

        // Get repository size (estimated from file count)
        long repositorySize = jGitService.getRepositorySize(owner, repoName);

        // Get issue and PR statistics
        long openIssues = issueService.countOpenIssues(repository.getId());
        long closedIssues = issueService.countClosedIssues(repository.getId());
        long openPullRequests = pullRequestService.countOpenPullRequests(repository.getId());
        long closedPullRequests = pullRequestService.countClosedPullRequests(repository.getId());
        long mergedPullRequests = pullRequestService.countMergedPullRequests(repository.getId());

        return RepositoryInsights.builder()
                .repositoryId(repository.getId())
                .owner(owner)
                .name(repoName)
                .totalCommits(commits.size())
                .commitsLastMonth(commitsLastMonth)
                .commitsLastWeek(commitsLastWeek)
                .totalBranches(totalBranches)
                .defaultBranch(defaultBranch)
                .totalFiles(totalFiles)
                .repositorySize(repositorySize)
                .openIssues(openIssues)
                .closedIssues(closedIssues)
                .openPullRequests(openPullRequests)
                .closedPullRequests(closedPullRequests)
                .mergedPullRequests(mergedPullRequests)
                .totalContributors(contributors.size())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private int countFiles(String owner, String repoName, String branch, String path) {
        try {
            return jGitService.getFiles(owner, repoName, branch, path).size();
        } catch (Exception e) {
            log.warn("Error counting files at path '{}': {}", path, e.getMessage());
            return 0;
        }
    }
}
