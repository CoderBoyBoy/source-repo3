package com.gitserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new repository.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRepositoryRequest {

    @NotBlank(message = "Repository name is required")
    @Size(min = 1, max = 100, message = "Repository name must be between 1 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private boolean isPrivate;

    private String defaultBranch;

    private boolean initReadme;
}
