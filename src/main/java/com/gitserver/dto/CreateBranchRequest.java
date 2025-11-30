package com.gitserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating a new branch.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBranchRequest {

    @NotBlank(message = "Branch name is required")
    private String name;

    @NotBlank(message = "Source branch or commit is required")
    private String source; // Source branch name or commit SHA
}
