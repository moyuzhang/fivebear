package com.fivebear.fivebear_system.exception;

public class IpPoolException extends RuntimeException {
    public IpPoolException(String message) {
        super(message);
    }

    public IpPoolException(String message, Throwable cause) {
        super(message, cause);
    }
} 