package com.gitserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO for creating an SSH key.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSshKeyRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be less than 100 characters")
    private String title;

    @NotBlank(message = "Public key is required")
    private String publicKey;
}
