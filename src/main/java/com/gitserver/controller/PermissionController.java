package com.gitserver.controller;

import com.gitserver.dto.PermissionRequest;
import com.gitserver.dto.PermissionResponse;
import com.gitserver.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for repository permission management.
 */
@RestController
@RequestMapping("/api/repos/{owner}/{repo}/collaborators")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "Repository permission management APIs")
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @Operation(summary = "Add a collaborator to a repository")
    public ResponseEntity<PermissionResponse> addCollaborator(
            @PathVariable String owner,
            @PathVariable String repo,
            @Valid @RequestBody PermissionRequest request) {
        PermissionResponse response = permissionService.addCollaborator(owner, repo, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all collaborators for a repository")
    public ResponseEntity<List<PermissionResponse>> getCollaborators(
            @PathVariable String owner,
            @PathVariable String repo) {
        return ResponseEntity.ok(permissionService.getCollaborators(owner, repo));
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get a specific collaborator's permission")
    public ResponseEntity<PermissionResponse> getCollaborator(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String username) {
        return ResponseEntity.ok(permissionService.getCollaborator(owner, repo, username));
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update a collaborator's permission")
    public ResponseEntity<PermissionResponse> updateCollaborator(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String username,
            @Valid @RequestBody PermissionRequest request) {
        request.setUsername(username);
        PermissionResponse response = permissionService.addCollaborator(owner, repo, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{username}")
    @Operation(summary = "Remove a collaborator from a repository")
    public ResponseEntity<Void> removeCollaborator(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String username) {
        permissionService.removeCollaborator(owner, repo, username);
        return ResponseEntity.noContent().build();
    }
}
