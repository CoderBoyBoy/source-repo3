package com.gitserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating/updating file content.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUpdateRequest {

    @NotBlank(message = "File path is required")
    private String path;

    @NotBlank(message = "Content is required")
    private String content;

    @NotBlank(message = "Commit message is required")
    private String message;

    private String branch;

    private String encoding; // "base64" or "utf-8", default is "utf-8"

    private String sha; // Required for updates, not for creates
}
