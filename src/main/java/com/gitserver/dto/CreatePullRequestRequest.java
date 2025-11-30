package com.gitserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Set;

/**
 * DTO for creating a pull request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePullRequestRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 256, message = "Title must be less than 256 characters")
    private String title;

    @Size(max = 65535, message = "Body is too long")
    private String body;

    @NotBlank(message = "Head branch is required")
    private String headBranch;

    @NotBlank(message = "Base branch is required")
    private String baseBranch;

    private Set<String> labels;

    private Set<String> reviewers;
}
