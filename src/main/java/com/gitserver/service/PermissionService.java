package com.gitserver.service;

import com.gitserver.dto.PermissionRequest;
import com.gitserver.dto.PermissionResponse;
import com.gitserver.entity.GitRepository;
import com.gitserver.entity.RepositoryPermission;
import com.gitserver.entity.RepositoryPermission.PermissionLevel;
import com.gitserver.exception.PermissionDeniedException;
import com.gitserver.exception.RepositoryNotFoundException;
import com.gitserver.exception.UserNotFoundException;
import com.gitserver.repository.GitRepositoryJpaRepository;
import com.gitserver.repository.RepositoryPermissionRepository;
import com.gitserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for repository permission management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final RepositoryPermissionRepository permissionRepository;
    private final GitRepositoryJpaRepository repositoryJpaRepository;
    private final UserRepository userRepository;

    /**
     * Add or update a collaborator's permission.
     */
    @Transactional
    public PermissionResponse addCollaborator(String owner, String repoName, PermissionRequest request) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, repoName)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, repoName));

        // Verify user exists
        if (!userRepository.existsByUsername(request.getUsername())) {
            throw new UserNotFoundException("User not found: " + request.getUsername());
        }

        // Cannot add owner as collaborator
        if (request.getUsername().equals(owner)) {
            throw new IllegalArgumentException("Cannot add repository owner as collaborator");
        }

        PermissionLevel level = PermissionLevel.valueOf(request.getPermission().toUpperCase());

        Optional<RepositoryPermission> existingPermission = 
                permissionRepository.findByRepositoryIdAndUsername(repository.getId(), request.getUsername());

        RepositoryPermission permission;
        if (existingPermission.isPresent()) {
            permission = existingPermission.get();
            permission.setPermission(level);
        } else {
            permission = RepositoryPermission.builder()
                    .repositoryId(repository.getId())
                    .username(request.getUsername())
                    .permission(level)
                    .build();
        }

        permission = permissionRepository.save(permission);
        log.info("Added/updated collaborator '{}' with {} permission on {}/{}", 
                 request.getUsername(), level, owner, repoName);

        return toResponse(permission);
    }

    /**
     * Get all collaborators for a repository.
     */
    public List<PermissionResponse> getCollaborators(String owner, String repoName) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, repoName)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, repoName));

        return permissionRepository.findByRepositoryId(repository.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific collaborator's permission.
     */
    public PermissionResponse getCollaborator(String owner, String repoName, String username) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, repoName)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, repoName));

        RepositoryPermission permission = permissionRepository
                .findByRepositoryIdAndUsername(repository.getId(), username)
                .orElseThrow(() -> new PermissionDeniedException(username, owner, repoName, "access"));

        return toResponse(permission);
    }

    /**
     * Remove a collaborator.
     */
    @Transactional
    public void removeCollaborator(String owner, String repoName, String username) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, repoName)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, repoName));

        permissionRepository.deleteByRepositoryIdAndUsername(repository.getId(), username);
        log.info("Removed collaborator '{}' from {}/{}", username, owner, repoName);
    }

    /**
     * Check if a user has at least the specified permission level.
     */
    public boolean hasPermission(String owner, String repoName, String username, PermissionLevel requiredLevel) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, repoName)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, repoName));

        // Owner always has ADMIN permission
        if (username.equals(owner)) {
            return true;
        }

        // Check for public repository and READ access
        if (!repository.isPrivate() && requiredLevel == PermissionLevel.READ) {
            return true;
        }

        Optional<RepositoryPermission> permission = 
                permissionRepository.findByRepositoryIdAndUsername(repository.getId(), username);

        if (permission.isEmpty()) {
            return false;
        }

        return hasRequiredLevel(permission.get().getPermission(), requiredLevel);
    }

    /**
     * Check if a user can write to a repository.
     */
    public boolean canWrite(String owner, String repoName, String username) {
        return hasPermission(owner, repoName, username, PermissionLevel.WRITE);
    }

    /**
     * Check if a user can administer a repository.
     */
    public boolean canAdmin(String owner, String repoName, String username) {
        return hasPermission(owner, repoName, username, PermissionLevel.ADMIN);
    }

    /**
     * Verify user has permission, throw exception if not.
     */
    public void verifyPermission(String owner, String repoName, String username, 
                                  PermissionLevel requiredLevel, String action) {
        if (!hasPermission(owner, repoName, username, requiredLevel)) {
            throw new PermissionDeniedException(username, owner, repoName, action);
        }
    }

    private boolean hasRequiredLevel(PermissionLevel actual, PermissionLevel required) {
        // ADMIN > WRITE > READ
        return switch (required) {
            case READ -> true; // All permission levels include read
            case WRITE -> actual == PermissionLevel.WRITE || actual == PermissionLevel.ADMIN;
            case ADMIN -> actual == PermissionLevel.ADMIN;
        };
    }

    private PermissionResponse toResponse(RepositoryPermission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .repositoryId(permission.getRepositoryId())
                .username(permission.getUsername())
                .permission(permission.getPermission().name())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }
}
