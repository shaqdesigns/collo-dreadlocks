package com.collo.dreadlocks.notification;

public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(Integer id) {
        super("Notification not found: " + id);
    }
}
