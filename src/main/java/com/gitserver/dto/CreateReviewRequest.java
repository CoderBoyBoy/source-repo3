package com.gitserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO for creating a review.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReviewRequest {

    @Size(max = 65535, message = "Body is too long")
    private String body;

    @NotBlank(message = "Event is required (APPROVE, REQUEST_CHANGES, COMMENT)")
    private String event;

    private String commitId;
}
