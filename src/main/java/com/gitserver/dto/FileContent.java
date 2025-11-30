package com.gitserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO for file content.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileContent {

    private String name;
    private String path;
    private String content;
    private String encoding; // "base64" or "utf-8"
    private long size;
    private String sha;
}
