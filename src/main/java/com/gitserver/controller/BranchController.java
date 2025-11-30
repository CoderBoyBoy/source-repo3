package com.gitserver.controller;

import com.gitserver.dto.*;
import com.gitserver.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for branch operations.
 */
@RestController
@RequestMapping("/api/repos/{owner}/{repo}/branches")
@RequiredArgsConstructor
@Tag(name = "Branch", description = "Branch management APIs")
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    @Operation(summary = "Get all branches in a repository")
    public ResponseEntity<List<BranchInfo>> getBranches(
            @PathVariable String owner,
            @PathVariable String repo) {
        return ResponseEntity.ok(branchService.getBranches(owner, repo));
    }

    @GetMapping("/{branch}")
    @Operation(summary = "Get a specific branch")
    public ResponseEntity<BranchInfo> getBranch(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String branch) {
        return ResponseEntity.ok(branchService.getBranch(owner, repo, branch));
    }

    @PostMapping
    @Operation(summary = "Create a new branch")
    public ResponseEntity<BranchInfo> createBranch(
            @PathVariable String owner,
            @PathVariable String repo,
            @Valid @RequestBody CreateBranchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(branchService.createBranch(owner, repo, request));
    }

    @DeleteMapping("/{branch}")
    @Operation(summary = "Delete a branch")
    public ResponseEntity<Void> deleteBranch(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String branch) {
        branchService.deleteBranch(owner, repo, branch);
        return ResponseEntity.noContent().build();
    }
}
