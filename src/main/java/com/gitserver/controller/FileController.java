package com.gitserver.controller;

import com.gitserver.dto.*;
import com.gitserver.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for file operations.
 */
@RestController
@RequestMapping("/api/repos/{owner}/{repo}")
@RequiredArgsConstructor
@Tag(name = "File", description = "File management APIs")
public class FileController {

    private final FileService fileService;

    @GetMapping("/contents")
    @Operation(summary = "Get files in root directory")
    public ResponseEntity<List<FileInfo>> getRootFiles(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam(required = false) String branch) {
        return ResponseEntity.ok(fileService.getFiles(owner, repo, branch, ""));
    }

    @GetMapping("/contents/{*path}")
    @Operation(summary = "Get files in a directory or file content")
    public ResponseEntity<?> getContents(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String path,
            @RequestParam(required = false) String branch) {
        
        // Try to get as file first
        try {
            FileContent content = fileService.getFileContent(owner, repo, branch, path);
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            // If not a file, try as directory
            List<FileInfo> files = fileService.getFiles(owner, repo, branch, path);
            return ResponseEntity.ok(files);
        }
    }

    @PutMapping("/contents/{*path}")
    @Operation(summary = "Create or update a file")
    public ResponseEntity<CommitInfo> createOrUpdateFile(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String path,
            @Valid @RequestBody FileUpdateRequest request,
            Authentication authentication) {
        
        request.setPath(path);
        String authorName = authentication.getName();
        String authorEmail = authorName + "@gitserver.local";
        
        CommitInfo commit = fileService.createOrUpdateFile(owner, repo, request, authorName, authorEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(commit);
    }

    @DeleteMapping("/contents/{*path}")
    @Operation(summary = "Delete a file")
    public ResponseEntity<CommitInfo> deleteFile(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String path,
            @RequestParam(required = false) String branch,
            @RequestParam String message,
            Authentication authentication) {
        
        String authorName = authentication.getName();
        String authorEmail = authorName + "@gitserver.local";
        
        CommitInfo commit = fileService.deleteFile(owner, repo, path, branch, message, authorName, authorEmail);
        return ResponseEntity.ok(commit);
    }
}
