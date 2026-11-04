package com.collo.dreadlocks.customer;

public class CustomerConflictException extends RuntimeException {

    public CustomerConflictException(String message) {
        super(message);
    }
}
