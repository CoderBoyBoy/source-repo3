package com.gitserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO for repository response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryResponse {

    private Long id;
    private String name;
    private String description;
    private String owner;
    private boolean isPrivate;
    private String defaultBranch;
    private String cloneUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
