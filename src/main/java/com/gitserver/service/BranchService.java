package com.gitserver.service;

import com.gitserver.dto.*;
import com.gitserver.entity.GitRepository;
import com.gitserver.exception.RepositoryNotFoundException;
import com.gitserver.git.JGitService;
import com.gitserver.repository.GitRepositoryJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for branch operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BranchService {

    private final GitRepositoryJpaRepository repositoryJpaRepository;
    private final JGitService jGitService;

    /**
     * Get all branches in a repository.
     */
    public List<BranchInfo> getBranches(String owner, String name) {
        validateRepository(owner, name);
        return jGitService.getBranches(owner, name);
    }

    /**
     * Get a specific branch.
     */
    public BranchInfo getBranch(String owner, String name, String branchName) {
        validateRepository(owner, name);
        return jGitService.getBranches(owner, name).stream()
                .filter(b -> b.getName().equals(branchName))
                .findFirst()
                .orElseThrow(() -> new com.gitserver.exception.BranchNotFoundException(name, branchName));
    }

    /**
     * Create a new branch.
     */
    public BranchInfo createBranch(String owner, String name, CreateBranchRequest request) {
        validateRepository(owner, name);
        return jGitService.createBranch(owner, name, request.getName(), request.getSource());
    }

    /**
     * Delete a branch.
     */
    public void deleteBranch(String owner, String name, String branchName) {
        validateRepository(owner, name);
        jGitService.deleteBranch(owner, name, branchName);
    }

    private void validateRepository(String owner, String name) {
        if (!repositoryJpaRepository.existsByOwnerAndName(owner, name)) {
            throw new RepositoryNotFoundException(owner, name);
        }
    }
}
