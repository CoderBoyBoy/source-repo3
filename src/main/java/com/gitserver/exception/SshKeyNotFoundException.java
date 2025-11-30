package com.gitserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an SSH key is not found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class SshKeyNotFoundException extends RuntimeException {

    public SshKeyNotFoundException(String message) {
        super(message);
    }

    public SshKeyNotFoundException(Long keyId) {
        super(String.format("SSH key with ID %d not found", keyId));
    }
}
