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
 * Service for file operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final GitRepositoryJpaRepository repositoryJpaRepository;
    private final JGitService jGitService;

    /**
     * Get files in a directory.
     */
    public List<FileInfo> getFiles(String owner, String name, String branch, String path) {
        GitRepository repository = getRepository(owner, name);
        String effectiveBranch = branch != null ? branch : repository.getDefaultBranch();
        return jGitService.getFiles(owner, name, effectiveBranch, path);
    }

    /**
     * Get file content.
     */
    public FileContent getFileContent(String owner, String name, String branch, String path) {
        GitRepository repository = getRepository(owner, name);
        String effectiveBranch = branch != null ? branch : repository.getDefaultBranch();
        return jGitService.getFileContent(owner, name, effectiveBranch, path);
    }

    /**
     * Create or update a file.
     */
    public CommitInfo createOrUpdateFile(String owner, String name, FileUpdateRequest request, 
                                         String authorName, String authorEmail) {
        getRepository(owner, name);
        return jGitService.createOrUpdateFile(owner, name, request, authorName, authorEmail);
    }

    /**
     * Delete a file.
     */
    public CommitInfo deleteFile(String owner, String name, String path, String branch, 
                                 String message, String authorName, String authorEmail) {
        GitRepository repository = getRepository(owner, name);
        String effectiveBranch = branch != null ? branch : repository.getDefaultBranch();
        return jGitService.deleteFile(owner, name, path, effectiveBranch, message, authorName, authorEmail);
    }

    private GitRepository getRepository(String owner, String name) {
        return repositoryJpaRepository.findByOwnerAndName(owner, name)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, name));
    }
}
