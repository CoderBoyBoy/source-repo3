package com.gitserver.controller;

import com.gitserver.dto.*;
import com.gitserver.service.PullRequestService;
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
 * REST controller for pull request management.
 */
@RestController
@RequestMapping("/api/repos/{owner}/{repo}/pulls")
@RequiredArgsConstructor
@Tag(name = "Pull Requests", description = "Pull request management APIs")
public class PullRequestController {

    private final PullRequestService pullRequestService;

    @PostMapping
    @Operation(summary = "Create a new pull request")
    public ResponseEntity<PullRequestResponse> createPullRequest(
            @PathVariable String owner,
            @PathVariable String repo,
            @Valid @RequestBody CreatePullRequestRequest request,
            Authentication authentication) {
        String authorUsername = authentication.getName();
        PullRequestResponse response = pullRequestService.createPullRequest(owner, repo, authorUsername, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all pull requests for a repository")
    public ResponseEntity<List<PullRequestResponse>> getPullRequests(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam(required = false) String state) {
        return ResponseEntity.ok(pullRequestService.getPullRequests(owner, repo, state));
    }

    @GetMapping("/{prNumber}")
    @Operation(summary = "Get a pull request by number")
    public ResponseEntity<PullRequestResponse> getPullRequest(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable Integer prNumber) {
        return ResponseEntity.ok(pullRequestService.getPullRequest(owner, repo, prNumber));
    }

    @PatchMapping("/{prNumber}")
    @Operation(summary = "Update a pull request")
    public ResponseEntity<PullRequestResponse> updatePullRequest(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable Integer prNumber,
            @Valid @RequestBody UpdatePullRequestRequest request) {
        return ResponseEntity.ok(pullRequestService.updatePullRequest(owner, repo, prNumber, request));
    }

    @PutMapping("/{prNumber}/merge")
    @Operation(summary = "Merge a pull request")
    public ResponseEntity<PullRequestResponse> mergePullRequest(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable Integer prNumber,
            Authentication authentication) {
        String mergedBy = authentication.getName();
        return ResponseEntity.ok(pullRequestService.mergePullRequest(owner, repo, prNumber, mergedBy));
    }

    @PutMapping("/{prNumber}/close")
    @Operation(summary = "Close a pull request")
    public ResponseEntity<PullRequestResponse> closePullRequest(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable Integer prNumber) {
        return ResponseEntity.ok(pullRequestService.closePullRequest(owner, repo, prNumber));
    }

    @GetMapping("/{prNumber}/diff")
    @Operation(summary = "Get the diff for a pull request")
    public ResponseEntity<String> getPullRequestDiff(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable Integer prNumber) {
        return ResponseEntity.ok(pullRequestService.getPullRequestDiff(owner, repo, prNumber));
    }
}
