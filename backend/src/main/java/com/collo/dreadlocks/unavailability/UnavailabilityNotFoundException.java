package com.collo.dreadlocks.unavailability;

public class UnavailabilityNotFoundException extends RuntimeException {

    public UnavailabilityNotFoundException(Integer id) {
        super("Unavailability request not found: " + id);
    }
}
