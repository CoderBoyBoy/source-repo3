package com.gitserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * Entity representing user permissions for a repository.
 */
@Entity
@Table(name = "repository_permissions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"repository_id", "username"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repository_id", nullable = false)
    private Long repositoryId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PermissionLevel permission;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum PermissionLevel {
        READ,   // Can view and clone repository
        WRITE,  // Can push to repository
        ADMIN   // Can manage repository settings and permissions
    }
}
