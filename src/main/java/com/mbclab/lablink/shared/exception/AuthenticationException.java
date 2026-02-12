package com.mbclab.lablink.shared.exception;

/**
 * Exception for authentication-related errors.
 * Thrown when login fails, token is invalid, or password change fails.
 */
public class AuthenticationException extends RuntimeException {
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
