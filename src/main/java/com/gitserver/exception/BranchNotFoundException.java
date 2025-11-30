package com.gitserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a branch is not found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class BranchNotFoundException extends RuntimeException {

    public BranchNotFoundException(String message) {
        super(message);
    }

    public BranchNotFoundException(String repository, String branch) {
        super(String.format("Branch '%s' not found in repository '%s'", branch, repository));
    }
}
