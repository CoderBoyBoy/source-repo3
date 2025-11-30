package com.gitserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for issue response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueResponse {

    private Long id;
    private Integer issueNumber;
    private String title;
    private String body;
    private String state;
    private String author;
    private String assignee;
    private Set<String> labels;
    private String repositoryOwner;
    private String repositoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
}
