package com.gitserver.controller;

import com.gitserver.dto.*;
import com.gitserver.service.SshKeyService;
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
 * REST controller for SSH key management.
 */
@RestController
@RequestMapping("/api/users/ssh-keys")
@RequiredArgsConstructor
@Tag(name = "SSH Keys", description = "SSH key management APIs")
public class SshKeyController {

    private final SshKeyService sshKeyService;

    @PostMapping
    @Operation(summary = "Add a new SSH key")
    public ResponseEntity<SshKeyResponse> addSshKey(
            @Valid @RequestBody CreateSshKeyRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        SshKeyResponse response = sshKeyService.addSshKey(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all SSH keys for the current user")
    public ResponseEntity<List<SshKeyResponse>> getSshKeys(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(sshKeyService.getSshKeys(username));
    }

    @GetMapping("/{keyId}")
    @Operation(summary = "Get an SSH key by ID")
    public ResponseEntity<SshKeyResponse> getSshKey(
            @PathVariable Long keyId,
            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(sshKeyService.getSshKey(username, keyId));
    }

    @DeleteMapping("/{keyId}")
    @Operation(summary = "Delete an SSH key")
    public ResponseEntity<Void> deleteSshKey(
            @PathVariable Long keyId,
            Authentication authentication) {
        String username = authentication.getName();
        sshKeyService.deleteSshKey(username, keyId);
        return ResponseEntity.noContent().build();
    }
}
