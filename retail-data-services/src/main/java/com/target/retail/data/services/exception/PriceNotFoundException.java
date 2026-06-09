package com.target.retail.data.services.exception;

public class PriceNotFoundException extends RuntimeException {
    public PriceNotFoundException(String id) {
        super("Price not found for item id " + id);
    }
}
