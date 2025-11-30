package com.gitserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a pull request is not found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PullRequestNotFoundException extends RuntimeException {

    public PullRequestNotFoundException(String message) {
        super(message);
    }

    public PullRequestNotFoundException(String owner, String repo, Integer prNumber) {
        super(String.format("Pull request #%d not found in repository '%s/%s'", prNumber, owner, repo));
    }
}
