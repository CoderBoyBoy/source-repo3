package com.gitserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user already exists.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public static UserAlreadyExistsException forUsername(String username) {
        return new UserAlreadyExistsException(
            String.format("User with username '%s' already exists", username));
    }

    public static UserAlreadyExistsException forEmail(String email) {
        return new UserAlreadyExistsException(
            String.format("User with email '%s' already exists", email));
    }
}
