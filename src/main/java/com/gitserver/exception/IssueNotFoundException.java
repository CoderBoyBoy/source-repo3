package com.gitserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an issue is not found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class IssueNotFoundException extends RuntimeException {

    public IssueNotFoundException(String message) {
        super(message);
    }

    public IssueNotFoundException(String owner, String repo, Integer issueNumber) {
        super(String.format("Issue #%d not found in repository '%s/%s'", issueNumber, owner, repo));
    }
}
