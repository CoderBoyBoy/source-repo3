package com.gitserver.service;

import com.gitserver.dto.*;
import com.gitserver.entity.GitRepository;
import com.gitserver.exception.RepositoryAlreadyExistsException;
import com.gitserver.exception.RepositoryNotFoundException;
import com.gitserver.git.JGitService;
import com.gitserver.repository.GitRepositoryJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for repository management operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryService {

    private final GitRepositoryJpaRepository repositoryJpaRepository;
    private final JGitService jGitService;

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${git.repositories.default-branch:main}")
    private String defaultBranch;

    /**
     * Create a new repository.
     */
    @Transactional
    public RepositoryResponse createRepository(String owner, CreateRepositoryRequest request) {
        // Check if repository already exists
        if (repositoryJpaRepository.existsByOwnerAndName(owner, request.getName())) {
            throw new RepositoryAlreadyExistsException(owner, request.getName());
        }

        // Initialize Git repository on disk
        jGitService.initRepository(
            owner, 
            request.getName(), 
            request.isInitReadme(),
            request.getDescription()
        );

        // Save to database
        String branch = request.getDefaultBranch() != null ? request.getDefaultBranch() : defaultBranch;
        
        GitRepository repository = GitRepository.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .isPrivate(request.isPrivate())
                .defaultBranch(branch)
                .diskPath(jGitService.getDiskPath(owner, request.getName()))
                .build();

        repository = repositoryJpaRepository.save(repository);
        log.info("Created repository: {}/{}", owner, request.getName());

        return toResponse(repository);
    }

    /**
     * Get a repository by owner and name.
     */
    public RepositoryResponse getRepository(String owner, String name) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, name)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, name));
        return toResponse(repository);
    }

    /**
     * Get all repositories for an owner.
     */
    public List<RepositoryResponse> getRepositoriesByOwner(String owner) {
        return repositoryJpaRepository.findByOwner(owner).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all public repositories.
     */
    public List<RepositoryResponse> getPublicRepositories() {
        return repositoryJpaRepository.findByIsPrivate(false).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all repositories.
     */
    public List<RepositoryResponse> getAllRepositories() {
        return repositoryJpaRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update a repository.
     */
    @Transactional
    public RepositoryResponse updateRepository(String owner, String name, CreateRepositoryRequest request) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, name)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, name));

        if (request.getDescription() != null) {
            repository.setDescription(request.getDescription());
        }
        repository.setPrivate(request.isPrivate());
        if (request.getDefaultBranch() != null) {
            repository.setDefaultBranch(request.getDefaultBranch());
        }

        repository = repositoryJpaRepository.save(repository);
        log.info("Updated repository: {}/{}", owner, name);

        return toResponse(repository);
    }

    /**
     * Delete a repository.
     */
    @Transactional
    public void deleteRepository(String owner, String name) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, name)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, name));

        // Delete from disk
        jGitService.deleteRepository(owner, name);

        // Delete from database
        repositoryJpaRepository.delete(repository);
        log.info("Deleted repository: {}/{}", owner, name);
    }

    /**
     * Check if a repository exists.
     */
    public boolean repositoryExists(String owner, String name) {
        return repositoryJpaRepository.existsByOwnerAndName(owner, name);
    }

    private RepositoryResponse toResponse(GitRepository repository) {
        String cloneUrl = String.format("http://localhost:%d/git/%s/%s.git", 
                serverPort, repository.getOwner(), repository.getName());

        return RepositoryResponse.builder()
                .id(repository.getId())
                .name(repository.getName())
                .description(repository.getDescription())
                .owner(repository.getOwner())
                .isPrivate(repository.isPrivate())
                .defaultBranch(repository.getDefaultBranch())
                .cloneUrl(cloneUrl)
                .createdAt(repository.getCreatedAt())
                .updatedAt(repository.getUpdatedAt())
                .build();
    }
}
