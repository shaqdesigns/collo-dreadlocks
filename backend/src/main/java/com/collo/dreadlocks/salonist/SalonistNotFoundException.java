package com.collo.dreadlocks.salonist;

public class SalonistNotFoundException extends RuntimeException {

    public SalonistNotFoundException(Integer id) {
        super("Salonist not found: " + id);
    }
}
