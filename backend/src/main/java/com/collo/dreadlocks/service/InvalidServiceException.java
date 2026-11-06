package com.collo.dreadlocks.service;

public class InvalidServiceException extends RuntimeException {

    public InvalidServiceException(String message) {
        super(message);
    }
}
