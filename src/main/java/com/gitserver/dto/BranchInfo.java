package com.gitserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO for branch information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchInfo {

    private String name;
    private String commitId;
    private String commitMessage;
    private String author;
    private String authorEmail;
    private long commitTime;
    private boolean isDefault;
}
