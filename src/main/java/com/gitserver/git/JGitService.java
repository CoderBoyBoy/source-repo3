package com.gitserver.git;

import com.gitserver.dto.*;
import com.gitserver.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Service for Git operations using JGit.
 */
@Slf4j
@Service
public class JGitService {

    @Value("${git.repositories.base-path:./repositories}")
    private String repositoriesBasePath;

    @Value("${git.repositories.default-branch:main}")
    private String defaultBranch;

    /**
     * Initialize a new bare Git repository.
     */
    public void initRepository(String owner, String name, boolean initReadme, String description) {
        Path repoPath = getRepositoryPath(owner, name);
        
        try {
            Files.createDirectories(repoPath);
            
            // Initialize bare repository
            try (Git git = Git.init()
                    .setDirectory(repoPath.toFile())
                    .setBare(false)
                    .setInitialBranch(defaultBranch)
                    .call()) {
                
                log.info("Initialized repository at: {}", repoPath);
                
                // Create initial commit with README if requested
                if (initReadme) {
                    String readmeContent = "# " + name + "\n\n";
                    if (description != null && !description.isEmpty()) {
                        readmeContent += description + "\n";
                    }
                    
                    Path readmePath = repoPath.resolve("README.md");
                    Files.writeString(readmePath, readmeContent);
                    
                    git.add().addFilepattern("README.md").call();
                    git.commit()
                        .setAuthor("System", "system@gitserver.local")
                        .setCommitter("System", "system@gitserver.local")
                        .setMessage("Initial commit")
                        .call();
                    
                    log.info("Created initial commit with README.md");
                }
            }
        } catch (IOException | GitAPIException e) {
            throw new GitOperationException("Failed to initialize repository: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a repository.
     */
    public void deleteRepository(String owner, String name) {
        Path repoPath = getRepositoryPath(owner, name);
        
        try {
            if (Files.exists(repoPath)) {
                deleteDirectory(repoPath.toFile());
                log.info("Deleted repository at: {}", repoPath);
            }
        } catch (IOException e) {
            throw new GitOperationException("Failed to delete repository: " + e.getMessage(), e);
        }
    }

    /**
     * Get list of branches in a repository.
     */
    public List<BranchInfo> getBranches(String owner, String name) {
        Path repoPath = getRepositoryPath(owner, name);
        List<BranchInfo> branches = new ArrayList<>();
        
        try (Git git = Git.open(repoPath.toFile())) {
            Repository repository = git.getRepository();
            String headBranch = repository.getBranch();
            
            List<Ref> branchRefs = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.ALL)
                    .call();
            
            for (Ref ref : branchRefs) {
                String branchName = extractBranchName(ref.getName());
                
                try (RevWalk revWalk = new RevWalk(repository)) {
                    ObjectId objectId = ref.getObjectId();
                    if (objectId != null) {
                        RevCommit commit = revWalk.parseCommit(objectId);
                        
                        branches.add(BranchInfo.builder()
                                .name(branchName)
                                .commitId(commit.getName())
                                .commitMessage(commit.getShortMessage())
                                .author(commit.getAuthorIdent().getName())
                                .authorEmail(commit.getAuthorIdent().getEmailAddress())
                                .commitTime(commit.getCommitTime() * 1000L)
                                .isDefault(branchName.equals(headBranch))
                                .build());
                    }
                }
            }
        } catch (IOException | GitAPIException e) {
            throw new GitOperationException("Failed to get branches: " + e.getMessage(), e);
        }
        
        return branches;
    }

    /**
     * Create a new branch.
     */
    public BranchInfo createBranch(String owner, String name, String branchName, String source) {
        Path repoPath = getRepositoryPath(owner, name);
        
        try (Git git = Git.open(repoPath.toFile())) {
            Repository repository = git.getRepository();
            
            // Resolve the source to a commit
            ObjectId sourceId = repository.resolve(source);
            if (sourceId == null) {
                sourceId = repository.resolve("refs/heads/" + source);
            }
            if (sourceId == null) {
                throw new BranchNotFoundException(name, source);
            }
            
            Ref newBranch = git.branchCreate()
                    .setName(branchName)
                    .setStartPoint(sourceId.getName())
                    .call();
            
            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(newBranch.getObjectId());
                
                return BranchInfo.builder()
                        .name(branchName)
                        .commitId(commit.getName())
                        .commitMessage(commit.getShortMessage())
                        .author(commit.getAuthorIdent().getName())
                        .authorEmail(commit.getAuthorIdent().getEmailAddress())
                        .commitTime(commit.getCommitTime() * 1000L)
                        .isDefault(false)
                        .build();
            }
        } catch (IOException | GitAPIException e) {
            throw new GitOperationException("Failed to create branch: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a branch.
     */
    public void deleteBranch(String owner, String name, String branchName) {
        Path repoPath = getRepositoryPath(owner, name);
        
        try (Git git = Git.open(repoPath.toFile())) {
            git.branchDelete()
                    .setBranchNames(branchName)
                    .setForce(true)
                    .call();
            
            log.info("Deleted branch '{}' from repository '{}/{}'", branchName, owner, name);
        } catch (IOException | GitAPIException e) {
            throw new GitOperationException("Failed to delete branch: " + e.getMessage(), e);
        }
    }

    /**
     * Get commits in a branch.
     */
    public List<CommitInfo> getCommits(String owner, String name, String branch, int limit) {
        Path repoPath = getRepositoryPath(owner, name);
        List<CommitInfo> commits = new ArrayList<>();
        
        try (Git git = Git.open(repoPath.toFile())) {
            Repository repository = git.getRepository();
            
            ObjectId branchId = repository.resolve("refs/heads/" + branch);
            if (branchId == null) {
                branchId = repository.resolve(branch);
            }
            if (branchId == null) {
                throw new BranchNotFoundException(name, branch);
            }
            
            Iterable<RevCommit> log = git.log()
                    .add(branchId)
                    .setMaxCount(limit)
                    .call();
            
            for (RevCommit commit : log) {
                List<String> parentIds = new ArrayList<>();
                for (RevCommit parent : commit.getParents()) {
                    parentIds.add(parent.getName());
                }
                
                commits.add(CommitInfo.builder()
                        .id(commit.getName())
                        .shortId(commit.abbreviate(7).name())
                        .message(commit.getFullMessage())
                        .author(commit.getAuthorIdent().getName())
                        .authorEmail(commit.getAuthorIdent().getEmailAddress())
                        .authorTime(commit.getAuthorIdent().getWhen().getTime())
                        .committer(commit.getCommitterIdent().getName())
                        .committerEmail(commit.getCommitterIdent().getEmailAddress())
                        .commitTime(commit.getCommitTime() * 1000L)
                        .parentIds(parentIds)
                        .build());
            }
        } catch (IOException | GitAPIException e) {
            throw new GitOperationException("Failed to get commits: " + e.getMessage(), e);
        }
        
        return commits;
    }

    /**
     * Get a specific commit.
     */
    public CommitInfo getCommit(String owner, String name, String commitId) {
        Path repoPath = getRepositoryPath(owner, name);
        
        try (Git git = Git.open(repoPath.toFile())) {
            Repository repository = git.getRepository();
            
            ObjectId objectId = repository.resolve(commitId);
            if (objectId == null) {
                throw new GitOperationException("Commit not found: " + commitId);
            }
            
            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(objectId);
                
                List<String> parentIds = new ArrayList<>();
                for (RevCommit parent : commit.getParents()) {
                    parentIds.add(parent.getName());
                }
                
                return CommitInfo.builder()
                        .id(commit.getName())
                        .shortId(commit.abbreviate(7).name())
                        .message(commit.getFullMessage())
                        .author(commit.getAuthorIdent().getName())
                        .authorEmail(commit.getAuthorIdent().getEmailAddress())
                        .authorTime(commit.getAuthorIdent().getWhen().getTime())
                        .committer(commit.getCommitterIdent().getName())
                        .committerEmail(commit.getCommitterIdent().getEmailAddress())
                        .commitTime(commit.getCommitTime() * 1000L)
                        .parentIds(parentIds)
                        .build();
            }
        } catch (IOException e) {
            throw new GitOperationException("Failed to get commit: " + e.getMessage(), e);
        }
    }

    /**
     * Get list of files in a directory.
     */
    public List<FileInfo> getFiles(String owner, String name, String branch, String path) {
        Path repoPath = getRepositoryPath(owner, name);
        List<FileInfo> files = new ArrayList<>();
        
        try (Git git = Git.open(repoPath.toFile())) {
            Repository repository = git.getRepository();
            
            ObjectId branchId = repository.resolve("refs/heads/" + branch);
            if (branchId == null) {
                branchId = repository.resolve(branch);
            }
            if (branchId == null) {
                throw new BranchNotFoundException(name, branch);
            }
            
            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(branchId);
                RevTree tree = commit.getTree();
                
                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(false);
                    
                    if (path != null && !path.isEmpty() && !path.equals("/")) {
                        treeWalk.setFilter(PathFilter.create(path));
                    }
                    
                    while (treeWalk.next()) {
                        String filePath = treeWalk.getPathString();
                        
                        // If path is specified, only include direct children
                        if (path != null && !path.isEmpty() && !path.equals("/")) {
                            if (!filePath.startsWith(path + "/") && !filePath.equals(path)) {
                                continue;
                            }
                            // Skip if it's a nested file
                            String relativePath = filePath.substring(path.length());
                            if (relativePath.startsWith("/")) {
                                relativePath = relativePath.substring(1);
                            }
                            if (relativePath.contains("/")) {
                                continue;
                            }
                        }
                        
                        FileMode fileMode = treeWalk.getFileMode(0);
                        ObjectId objectId = treeWalk.getObjectId(0);
                        
                        String type = fileMode == FileMode.TREE ? "directory" : "file";
                        long size = 0;
                        
                        if (fileMode != FileMode.TREE) {
                            ObjectLoader loader = repository.open(objectId);
                            size = loader.getSize();
                        }
                        
                        files.add(FileInfo.builder()
                                .name(treeWalk.getNameString())
                                .path(filePath)
                                .type(type)
                                .size(size)
                                .mode(Integer.toOctalString(fileMode.getBits()))
                                .sha(objectId.getName())
                                .build());
                    }
                }
            }
        } catch (IOException e) {
            throw new GitOperationException("Failed to get files: " + e.getMessage(), e);
        }
        
        return files;
    }

    /**
     * Get file content.
     */
    public FileContent getFileContent(String owner, String name, String branch, String path) {
        Path repoPath = getRepositoryPath(owner, name);
        
        try (Git git = Git.open(repoPath.toFile())) {
            Repository repository = git.getRepository();
            
            ObjectId branchId = repository.resolve("refs/heads/" + branch);
            if (branchId == null) {
                branchId = repository.resolve(branch);
            }
            if (branchId == null) {
                throw new BranchNotFoundException(name, branch);
            }
            
            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(branchId);
                RevTree tree = commit.getTree();
                
                try (TreeWalk treeWalk = TreeWalk.forPath(repository, path, tree)) {
                    if (treeWalk == null) {
                        throw new FileNotFoundException(name, path);
                    }
                    
                    ObjectId objectId = treeWalk.getObjectId(0);
                    ObjectLoader loader = repository.open(objectId);
                    
                    byte[] bytes = loader.getBytes();
                    String content;
                    String encoding;
                    
                    // Try to decode as UTF-8, fall back to Base64
                    if (isTextContent(bytes)) {
                        content = new String(bytes, StandardCharsets.UTF_8);
                        encoding = "utf-8";
                    } else {
                        content = Base64.getEncoder().encodeToString(bytes);
                        encoding = "base64";
                    }
                    
                    return FileContent.builder()
                            .name(treeWalk.getNameString())
                            .path(path)
                            .content(content)
                            .encoding(encoding)
                            .size(loader.getSize())
                            .sha(objectId.getName())
                            .build();
                }
            }
        } catch (IOException e) {
            throw new GitOperationException("Failed to get file content: " + e.getMessage(), e);
        }
    }

    /**
     * Create or update a file.
     */
    public CommitInfo createOrUpdateFile(String owner, String name, FileUpdateRequest request, 
                                         String authorName, String authorEmail) {
        Path repoPath = getRepositoryPath(owner, name);
        String branch = request.getBranch() != null ? request.getBranch() : defaultBranch;
        
        try (Git git = Git.open(repoPath.toFile())) {
            // Checkout the branch
            git.checkout().setName(branch).call();
            
            // Write the file
            Path filePath = repoPath.resolve(request.getPath());
            Files.createDirectories(filePath.getParent());
            
            byte[] content;
            if ("base64".equals(request.getEncoding())) {
                content = Base64.getDecoder().decode(request.getContent());
            } else {
                content = request.getContent().getBytes(StandardCharsets.UTF_8);
            }
            Files.write(filePath, content);
            
            // Add and commit
            git.add().addFilepattern(request.getPath()).call();
            
            RevCommit commit = git.commit()
                    .setAuthor(authorName, authorEmail)
                    .setCommitter(authorName, authorEmail)
                    .setMessage(request.getMessage())
                    .call();
            
            List<String> parentIds = new ArrayList<>();
            for (RevCommit parent : commit.getParents()) {
                parentIds.add(parent.getName());
            }
            
            return CommitInfo.builder()
                    .id(commit.getName())
                    .shortId(commit.abbreviate(7).name())
                    .message(commit.getFullMessage())
                    .author(commit.getAuthorIdent().getName())
                    .authorEmail(commit.getAuthorIdent().getEmailAddress())
                    .authorTime(commit.getAuthorIdent().getWhen().getTime())
                    .committer(commit.getCommitterIdent().getName())
                    .committerEmail(commit.getCommitterIdent().getEmailAddress())
                    .commitTime(commit.getCommitTime() * 1000L)
                    .parentIds(parentIds)
                    .build();
                    
        } catch (IOException | GitAPIException e) {
            throw new GitOperationException("Failed to create/update file: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a file.
     */
    public CommitInfo deleteFile(String owner, String name, String path, String branch, 
                                 String message, String authorName, String authorEmail) {
        Path repoPath = getRepositoryPath(owner, name);
        
        try (Git git = Git.open(repoPath.toFile())) {
            // Checkout the branch
            git.checkout().setName(branch).call();
            
            // Delete the file
            Path filePath = repoPath.resolve(path);
            if (!Files.exists(filePath)) {
                throw new FileNotFoundException(name, path);
            }
            Files.delete(filePath);
            
            // Add deletion and commit
            git.rm().addFilepattern(path).call();
            
            RevCommit commit = git.commit()
                    .setAuthor(authorName, authorEmail)
                    .setCommitter(authorName, authorEmail)
                    .setMessage(message)
                    .call();
            
            List<String> parentIds = new ArrayList<>();
            for (RevCommit parent : commit.getParents()) {
                parentIds.add(parent.getName());
            }
            
            return CommitInfo.builder()
                    .id(commit.getName())
                    .shortId(commit.abbreviate(7).name())
                    .message(commit.getFullMessage())
                    .author(commit.getAuthorIdent().getName())
                    .authorEmail(commit.getAuthorIdent().getEmailAddress())
                    .authorTime(commit.getAuthorIdent().getWhen().getTime())
                    .committer(commit.getCommitterIdent().getName())
                    .committerEmail(commit.getCommitterIdent().getEmailAddress())
                    .commitTime(commit.getCommitTime() * 1000L)
                    .parentIds(parentIds)
                    .build();
                    
        } catch (IOException | GitAPIException e) {
            throw new GitOperationException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    /**
     * Check if repository exists on disk.
     */
    public boolean repositoryExistsOnDisk(String owner, String name) {
        Path repoPath = getRepositoryPath(owner, name);
        return Files.exists(repoPath.resolve(".git")) || Files.exists(repoPath.resolve("HEAD"));
    }

    /**
     * Get the repository path on disk.
     */
    public Path getRepositoryPath(String owner, String name) {
        return Path.of(repositoriesBasePath, owner, name);
    }

    /**
     * Get the disk path for storing in database.
     */
    public String getDiskPath(String owner, String name) {
        return getRepositoryPath(owner, name).toString();
    }

    private String extractBranchName(String refName) {
        if (refName.startsWith("refs/heads/")) {
            return refName.substring("refs/heads/".length());
        }
        if (refName.startsWith("refs/remotes/")) {
            return refName.substring("refs/remotes/".length());
        }
        return refName;
    }

    private void deleteDirectory(File directory) throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (!file.delete()) {
                        throw new IOException("Failed to delete file: " + file.getAbsolutePath());
                    }
                }
            }
        }
        if (!directory.delete()) {
            throw new IOException("Failed to delete directory: " + directory.getAbsolutePath());
        }
    }

    private boolean isTextContent(byte[] bytes) {
        if (bytes.length == 0) {
            return true;
        }
        
        // Check for common binary signatures
        if (bytes.length >= 4) {
            // PNG
            if (bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
                return false;
            }
            // JPEG
            if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8) {
                return false;
            }
            // GIF
            if (bytes[0] == 0x47 && bytes[1] == 0x49 && bytes[2] == 0x46) {
                return false;
            }
            // PDF
            if (bytes[0] == 0x25 && bytes[1] == 0x50 && bytes[2] == 0x44 && bytes[3] == 0x46) {
                return false;
            }
        }
        
        // Check for null bytes (common in binary files)
        int nullCount = 0;
        int checkLength = Math.min(bytes.length, 8000);
        for (int i = 0; i < checkLength; i++) {
            if (bytes[i] == 0) {
                nullCount++;
                if (nullCount > checkLength / 100) {
                    return false;
                }
            }
        }
        
        return true;
    }
}
