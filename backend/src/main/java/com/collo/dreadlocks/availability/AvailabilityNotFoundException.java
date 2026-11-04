package com.collo.dreadlocks.availability;

public class AvailabilityNotFoundException extends RuntimeException {

    public AvailabilityNotFoundException(Integer id) {
        super("Availability not found: " + id);
    }
}
