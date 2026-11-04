package com.collo.dreadlocks.customer;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Integer id) {
        super("Customer not found: " + id);
    }
}
