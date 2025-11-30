package com.gitserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO for review comment response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCommentResponse {

    private Long id;
    private Long reviewId;
    private Long pullRequestId;
    private String author;
    private String body;
    private String filePath;
    private Integer lineNumber;
    private String commitId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
