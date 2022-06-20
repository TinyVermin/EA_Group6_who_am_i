package com.eleks.academy.whoami.core.exception;

public class TimeoutException extends RuntimeException {

    public TimeoutException(String message) {
        super(message);
    }

    public TimeoutException() {
        super();
    }
}
