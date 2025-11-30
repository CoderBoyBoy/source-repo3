package com.gitserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for pull request response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PullRequestResponse {

    private Long id;
    private Integer prNumber;
    private String title;
    private String body;
    private String state;
    private String author;
    private String headBranch;
    private String baseBranch;
    private Set<String> labels;
    private Set<String> reviewers;
    private boolean isMerged;
    private String mergedBy;
    private LocalDateTime mergedAt;
    private String repositoryOwner;
    private String repositoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
}
