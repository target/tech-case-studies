package com.target.retail.data.services.exception;

public class InducedFailureException extends RuntimeException {
    public InducedFailureException(String message) {
        super(message);
    }
}
