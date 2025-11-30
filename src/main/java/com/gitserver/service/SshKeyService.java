package com.gitserver.service;

import com.gitserver.dto.*;
import com.gitserver.entity.SshKey;
import com.gitserver.entity.User;
import com.gitserver.exception.InvalidSshKeyException;
import com.gitserver.exception.SshKeyAlreadyExistsException;
import com.gitserver.exception.SshKeyNotFoundException;
import com.gitserver.exception.UserNotFoundException;
import com.gitserver.repository.SshKeyRepository;
import com.gitserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for SSH key management operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SshKeyService {

    private final SshKeyRepository sshKeyRepository;
    private final UserRepository userRepository;

    /**
     * Add an SSH key for a user.
     */
    @Transactional
    public SshKeyResponse addSshKey(String username, CreateSshKeyRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        // Validate and parse the public key
        String[] keyParts = parsePublicKey(request.getPublicKey());
        String keyType = keyParts[0];
        String keyData = keyParts[1];

        // Calculate fingerprint
        String fingerprint = calculateFingerprint(keyData);

        // Check if key already exists
        if (sshKeyRepository.existsByFingerprint(fingerprint)) {
            throw new SshKeyAlreadyExistsException(fingerprint, true);
        }

        // Check if title already exists for this user
        if (sshKeyRepository.existsByUserIdAndTitle(user.getId(), request.getTitle())) {
            throw new SshKeyAlreadyExistsException(request.getTitle(), false);
        }

        SshKey sshKey = SshKey.builder()
                .userId(user.getId())
                .title(request.getTitle())
                .publicKey(request.getPublicKey().trim())
                .fingerprint(fingerprint)
                .keyType(keyType)
                .build();

        sshKey = sshKeyRepository.save(sshKey);
        log.info("Added SSH key '{}' for user '{}'", request.getTitle(), username);

        return toResponse(sshKey);
    }

    /**
     * Get all SSH keys for a user.
     */
    public List<SshKeyResponse> getSshKeys(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        return sshKeyRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get an SSH key by ID.
     */
    public SshKeyResponse getSshKey(String username, Long keyId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        SshKey sshKey = sshKeyRepository.findById(keyId)
                .orElseThrow(() -> new SshKeyNotFoundException(keyId));

        if (!sshKey.getUserId().equals(user.getId())) {
            throw new SshKeyNotFoundException(keyId);
        }

        return toResponse(sshKey);
    }

    /**
     * Delete an SSH key.
     */
    @Transactional
    public void deleteSshKey(String username, Long keyId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        SshKey sshKey = sshKeyRepository.findById(keyId)
                .orElseThrow(() -> new SshKeyNotFoundException(keyId));

        if (!sshKey.getUserId().equals(user.getId())) {
            throw new SshKeyNotFoundException(keyId);
        }

        sshKeyRepository.delete(sshKey);
        log.info("Deleted SSH key '{}' for user '{}'", sshKey.getTitle(), username);
    }

    /**
     * Parse and validate a public key.
     */
    private String[] parsePublicKey(String publicKey) {
        String trimmedKey = publicKey.trim();
        String[] parts = trimmedKey.split("\\s+");

        if (parts.length < 2) {
            throw new InvalidSshKeyException("Invalid SSH key format. Expected format: <key-type> <key-data> [comment]");
        }

        String keyType = parts[0];
        String keyData = parts[1];

        // Validate key type
        if (!keyType.startsWith("ssh-") && !keyType.startsWith("ecdsa-") && !keyType.equals("sk-ssh-ed25519@openssh.com")) {
            throw new InvalidSshKeyException("Unsupported SSH key type: " + keyType);
        }

        // Validate base64 encoding
        try {
            Base64.getDecoder().decode(keyData);
        } catch (IllegalArgumentException e) {
            throw new InvalidSshKeyException("Invalid SSH key data: not valid Base64 encoding");
        }

        return new String[]{keyType, keyData};
    }

    /**
     * Calculate the fingerprint of an SSH public key.
     */
    private String calculateFingerprint(String keyData) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(keyData);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(decodedKey);
            return "SHA256:" + Base64.getEncoder().encodeToString(hash).replace("=", "");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private SshKeyResponse toResponse(SshKey sshKey) {
        return SshKeyResponse.builder()
                .id(sshKey.getId())
                .title(sshKey.getTitle())
                .fingerprint(sshKey.getFingerprint())
                .keyType(sshKey.getKeyType())
                .lastUsedAt(sshKey.getLastUsedAt())
                .createdAt(sshKey.getCreatedAt())
                .build();
    }
}
