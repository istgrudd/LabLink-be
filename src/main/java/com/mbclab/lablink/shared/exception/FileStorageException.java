package com.mbclab.lablink.shared.exception;

/**
 * Exception for file storage-related errors.
 * Thrown when file upload, download, or storage operations fail.
 */
public class FileStorageException extends RuntimeException {
    
    public FileStorageException(String message) {
        super(message);
    }
    
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
