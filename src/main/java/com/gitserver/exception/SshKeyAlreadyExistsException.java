package com.gitserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an SSH key already exists.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class SshKeyAlreadyExistsException extends RuntimeException {

    public SshKeyAlreadyExistsException(String message) {
        super(message);
    }

    public SshKeyAlreadyExistsException(String fingerprint, boolean isFingerprint) {
        super(isFingerprint ? 
              String.format("SSH key with fingerprint '%s' already exists", fingerprint) :
              String.format("SSH key with title '%s' already exists", fingerprint));
    }
}
