package com.gitserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO for review response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Long id;
    private Long pullRequestId;
    private String reviewer;
    private String body;
    private String state;
    private String commitId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
