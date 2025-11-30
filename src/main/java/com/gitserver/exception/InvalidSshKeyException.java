package com.gitserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an invalid SSH key format is detected.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidSshKeyException extends RuntimeException {

    public InvalidSshKeyException(String message) {
        super(message);
    }
}
