package com.gitserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a repository already exists.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class RepositoryAlreadyExistsException extends RuntimeException {

    public RepositoryAlreadyExistsException(String message) {
        super(message);
    }

    public RepositoryAlreadyExistsException(String owner, String name) {
        super(String.format("Repository '%s/%s' already exists", owner, name));
    }
}
