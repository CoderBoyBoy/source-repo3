package com.gitserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO for file/directory information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileInfo {

    private String name;
    private String path;
    private String type; // "file" or "directory"
    private long size;
    private String mode;
    private String sha;
}
