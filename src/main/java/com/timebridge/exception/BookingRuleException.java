package com.timebridge.exception;

import org.springframework.http.HttpStatus;

public class BookingRuleException extends RuntimeException {

    private final HttpStatus status;

    public BookingRuleException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
