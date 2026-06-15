package com.target.retail.product.exception;

public class InducedFailureException extends RuntimeException {
    public InducedFailureException(String message) {
        super(message);
    }
}
