package com.gitserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO for SSH key response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SshKeyResponse {

    private Long id;
    private String title;
    private String fingerprint;
    private String keyType;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
}
