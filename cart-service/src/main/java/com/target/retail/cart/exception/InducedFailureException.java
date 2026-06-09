package com.target.retail.cart.exception;

public class InducedFailureException extends RuntimeException {
    public InducedFailureException(String message) {
        super(message);
    }
}
