package com.gitserver.controller;

import com.gitserver.dto.*;
import com.gitserver.service.CommitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for commit operations.
 */
@RestController
@RequestMapping("/api/repos/{owner}/{repo}/commits")
@RequiredArgsConstructor
@Tag(name = "Commit", description = "Commit management APIs")
public class CommitController {

    private final CommitService commitService;

    @GetMapping
    @Operation(summary = "Get commits in a branch")
    public ResponseEntity<List<CommitInfo>> getCommits(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam(defaultValue = "main") String branch,
            @RequestParam(defaultValue = "30") int limit) {
        return ResponseEntity.ok(commitService.getCommits(owner, repo, branch, limit));
    }

    @GetMapping("/{commitId}")
    @Operation(summary = "Get a specific commit")
    public ResponseEntity<CommitInfo> getCommit(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String commitId) {
        return ResponseEntity.ok(commitService.getCommit(owner, repo, commitId));
    }
}
