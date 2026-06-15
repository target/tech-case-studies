package com.target.retail.product.exception;

public class PriceNotFoundException extends RuntimeException {
    public PriceNotFoundException(String id) {
        super("Price not found for item id " + id);
    }
}
