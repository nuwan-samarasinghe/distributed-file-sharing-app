package com.assignment.distributedfilesharingapp.exception;

public class FileSharingException extends RuntimeException {
    public FileSharingException(String message) {
        super(message);
    }

    public FileSharingException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
