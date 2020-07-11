package com.assignment.distributedfilesharingapp.exception;

public class MessageExchangeException extends RuntimeException {
    public MessageExchangeException(String message) {
        super(message);
    }

    public MessageExchangeException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
