package com.gitserver.service;

import com.gitserver.dto.*;
import com.gitserver.exception.RepositoryNotFoundException;
import com.gitserver.git.JGitService;
import com.gitserver.repository.GitRepositoryJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for commit operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommitService {

    private final GitRepositoryJpaRepository repositoryJpaRepository;
    private final JGitService jGitService;

    /**
     * Get commits in a branch.
     */
    public List<CommitInfo> getCommits(String owner, String name, String branch, int limit) {
        validateRepository(owner, name);
        return jGitService.getCommits(owner, name, branch, limit);
    }

    /**
     * Get a specific commit.
     */
    public CommitInfo getCommit(String owner, String name, String commitId) {
        validateRepository(owner, name);
        return jGitService.getCommit(owner, name, commitId);
    }

    private void validateRepository(String owner, String name) {
        if (!repositoryJpaRepository.existsByOwnerAndName(owner, name)) {
            throw new RepositoryNotFoundException(owner, name);
        }
    }
}
