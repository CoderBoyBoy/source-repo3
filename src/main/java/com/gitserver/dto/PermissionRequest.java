package com.gitserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO for creating/updating repository permissions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotNull(message = "Permission level is required (READ, WRITE, ADMIN)")
    private String permission;
}
