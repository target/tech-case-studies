package com.target.retail.product.exception;

public class AvailabilityNotFoundException extends RuntimeException {
    public AvailabilityNotFoundException(String id) {
        super("Availability not found for item id " + id);
    }
}
