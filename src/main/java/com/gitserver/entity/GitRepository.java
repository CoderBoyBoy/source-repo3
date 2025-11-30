package com.gitserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * Entity representing a Git repository.
 */
@Entity
@Table(name = "repositories", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"owner", "name"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GitRepository {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String owner;

    @Column(name = "is_private")
    private boolean isPrivate;

    @Column(name = "default_branch")
    private String defaultBranch;

    @Column(name = "disk_path")
    private String diskPath;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (defaultBranch == null) {
            defaultBranch = "main";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
