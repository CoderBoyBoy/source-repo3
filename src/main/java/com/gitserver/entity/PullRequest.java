package com.gitserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entity representing a pull request in a repository.
 */
@Entity
@Table(name = "pull_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PullRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pr_number", nullable = false)
    private Integer prNumber;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PullRequestState state;

    @Column(name = "repository_id", nullable = false)
    private Long repositoryId;

    @Column(name = "author_username", nullable = false)
    private String authorUsername;

    @Column(name = "head_branch", nullable = false)
    private String headBranch;

    @Column(name = "base_branch", nullable = false)
    private String baseBranch;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pr_labels", joinColumns = @JoinColumn(name = "pr_id"))
    @Column(name = "label")
    private Set<String> labels;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pr_reviewers", joinColumns = @JoinColumn(name = "pr_id"))
    @Column(name = "reviewer")
    private Set<String> reviewers;

    @Column(name = "is_merged")
    private boolean isMerged;

    @Column(name = "merged_by")
    private String mergedBy;

    @Column(name = "merged_at")
    private LocalDateTime mergedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (state == null) {
            state = PullRequestState.OPEN;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum PullRequestState {
        OPEN, CLOSED, MERGED
    }
}
