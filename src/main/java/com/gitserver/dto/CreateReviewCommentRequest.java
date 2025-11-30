package com.gitserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO for creating a review comment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReviewCommentRequest {

    @NotBlank(message = "Body is required")
    @Size(max = 65535, message = "Body is too long")
    private String body;

    private String filePath;

    private Integer lineNumber;

    private String commitId;
}
