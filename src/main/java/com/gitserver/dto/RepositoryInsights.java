package com.gitserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO for repository insights/statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryInsights {

    private Long repositoryId;
    private String owner;
    private String name;

    // Commit statistics
    private int totalCommits;
    private int commitsLastMonth;
    private int commitsLastWeek;

    // Branch statistics
    private int totalBranches;
    private String defaultBranch;

    // File statistics
    private int totalFiles;
    private long repositorySize;

    // Issue statistics
    private long openIssues;
    private long closedIssues;

    // Pull request statistics
    private long openPullRequests;
    private long closedPullRequests;
    private long mergedPullRequests;

    // Contributor statistics
    private int totalContributors;

    // Timestamp
    private LocalDateTime generatedAt;
}
