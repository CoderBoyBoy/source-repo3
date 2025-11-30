package com.gitserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * DTO for commit information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitInfo {

    private String id;
    private String shortId;
    private String message;
    private String author;
    private String authorEmail;
    private long authorTime;
    private String committer;
    private String committerEmail;
    private long commitTime;
    private List<String> parentIds;
}
