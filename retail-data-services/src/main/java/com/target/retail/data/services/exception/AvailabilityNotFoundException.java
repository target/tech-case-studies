package com.target.retail.data.services.exception;

public class AvailabilityNotFoundException extends RuntimeException {
    public AvailabilityNotFoundException(String id) {
        super("Availability not found for item id " + id);
    }
}
