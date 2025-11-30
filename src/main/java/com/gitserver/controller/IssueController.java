package com.gitserver.controller;

import com.gitserver.dto.*;
import com.gitserver.service.IssueService;
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
 * REST controller for issue management.
 */
@RestController
@RequestMapping("/api/repos/{owner}/{repo}/issues")
@RequiredArgsConstructor
@Tag(name = "Issues", description = "Issue management APIs")
public class IssueController {

    private final IssueService issueService;

    @PostMapping
    @Operation(summary = "Create a new issue")
    public ResponseEntity<IssueResponse> createIssue(
            @PathVariable String owner,
            @PathVariable String repo,
            @Valid @RequestBody CreateIssueRequest request,
            Authentication authentication) {
        String authorUsername = authentication.getName();
        IssueResponse response = issueService.createIssue(owner, repo, authorUsername, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all issues for a repository")
    public ResponseEntity<List<IssueResponse>> getIssues(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam(required = false) String state) {
        return ResponseEntity.ok(issueService.getIssues(owner, repo, state));
    }

    @GetMapping("/{issueNumber}")
    @Operation(summary = "Get an issue by number")
    public ResponseEntity<IssueResponse> getIssue(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable Integer issueNumber) {
        return ResponseEntity.ok(issueService.getIssue(owner, repo, issueNumber));
    }

    @PatchMapping("/{issueNumber}")
    @Operation(summary = "Update an issue")
    public ResponseEntity<IssueResponse> updateIssue(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable Integer issueNumber,
            @Valid @RequestBody UpdateIssueRequest request) {
        return ResponseEntity.ok(issueService.updateIssue(owner, repo, issueNumber, request));
    }

    @PutMapping("/{issueNumber}/close")
    @Operation(summary = "Close an issue")
    public ResponseEntity<IssueResponse> closeIssue(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable Integer issueNumber) {
        return ResponseEntity.ok(issueService.closeIssue(owner, repo, issueNumber));
    }

    @PutMapping("/{issueNumber}/reopen")
    @Operation(summary = "Reopen an issue")
    public ResponseEntity<IssueResponse> reopenIssue(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable Integer issueNumber) {
        return ResponseEntity.ok(issueService.reopenIssue(owner, repo, issueNumber));
    }
}
