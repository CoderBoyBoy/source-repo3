package com.gitserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when permission is denied.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class PermissionDeniedException extends RuntimeException {

    public PermissionDeniedException(String message) {
        super(message);
    }

    public PermissionDeniedException(String username, String owner, String repo, String action) {
        super(String.format("User '%s' does not have permission to %s in repository '%s/%s'", 
              username, action, owner, repo));
    }
}
