package com.gitserver.controller;

import com.gitserver.dto.*;
import com.gitserver.service.RepositoryService;
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
 * REST controller for repository operations.
 */
@RestController
@RequestMapping("/api/repos")
@RequiredArgsConstructor
@Tag(name = "Repository", description = "Repository management APIs")
public class RepositoryController {

    private final RepositoryService repositoryService;

    @PostMapping
    @Operation(summary = "Create a new repository")
    public ResponseEntity<RepositoryResponse> createRepository(
            @Valid @RequestBody CreateRepositoryRequest request,
            Authentication authentication) {
        String owner = authentication.getName();
        RepositoryResponse response = repositoryService.createRepository(owner, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all repositories")
    public ResponseEntity<List<RepositoryResponse>> getAllRepositories() {
        return ResponseEntity.ok(repositoryService.getAllRepositories());
    }

    @GetMapping("/public")
    @Operation(summary = "Get all public repositories")
    public ResponseEntity<List<RepositoryResponse>> getPublicRepositories() {
        return ResponseEntity.ok(repositoryService.getPublicRepositories());
    }

    @GetMapping("/owner/{owner}")
    @Operation(summary = "Get repositories by owner")
    public ResponseEntity<List<RepositoryResponse>> getRepositoriesByOwner(@PathVariable String owner) {
        return ResponseEntity.ok(repositoryService.getRepositoriesByOwner(owner));
    }

    @GetMapping("/{owner}/{name}")
    @Operation(summary = "Get a repository by owner and name")
    public ResponseEntity<RepositoryResponse> getRepository(
            @PathVariable String owner,
            @PathVariable String name) {
        return ResponseEntity.ok(repositoryService.getRepository(owner, name));
    }

    @PutMapping("/{owner}/{name}")
    @Operation(summary = "Update a repository")
    public ResponseEntity<RepositoryResponse> updateRepository(
            @PathVariable String owner,
            @PathVariable String name,
            @Valid @RequestBody CreateRepositoryRequest request) {
        return ResponseEntity.ok(repositoryService.updateRepository(owner, name, request));
    }

    @DeleteMapping("/{owner}/{name}")
    @Operation(summary = "Delete a repository")
    public ResponseEntity<Void> deleteRepository(
            @PathVariable String owner,
            @PathVariable String name) {
        repositoryService.deleteRepository(owner, name);
        return ResponseEntity.noContent().build();
    }
}
