package com.jarvis.core.exception;

public class UpstreamBrainException extends RuntimeException {
    private final int statusCode;
    public UpstreamBrainException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
    public int getStatusCode() { return statusCode; }
}
