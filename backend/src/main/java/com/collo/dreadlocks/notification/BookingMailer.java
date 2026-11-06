package com.collo.dreadlocks.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class BookingMailer {

    private static final Logger log = LoggerFactory.getLogger(BookingMailer.class);

    private final MailProperties properties;
    private final SmtpClient smtpClient = new SmtpClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "booking-mail");
        thread.setDaemon(true);
        return thread;
    });

    public BookingMailer(MailProperties properties) {
        this.properties = properties;
        if (!isConfigured()) {
            log.info("Booking emails stay on the website until COLLO_SMTP_USERNAME and COLLO_SMTP_PASSWORD are set");
        }
    }

    public boolean isConfigured() {
        return properties.getUsername() != null
                && !properties.getUsername().isBlank()
                && properties.getPassword() != null
                && !properties.getPassword().isBlank();
    }

    public void send(String to, String subject, String body) {
        if (!isConfigured() || !deliverable(to)) {
            return;
        }
        String recipient = to.trim();
        Runnable delivery = () -> deliver(recipient, subject, body);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    executor.execute(delivery);
                }
            });
        } else {
            executor.execute(delivery);
        }
    }

    private void deliver(String to, String subject, String body) {
        try {
            smtpClient.send(
                    properties.getHost(),
                    properties.getPort(),
                    properties.getUsername().trim(),
                    properties.getPassword(),
                    fromAddress(),
                    to,
                    subject,
                    body
            );
        } catch (IOException | RuntimeException exception) {
            log.warn("Could not email {}: {}", to, exception.getMessage());
        }
    }

    private String fromAddress() {
        if (properties.getFrom() != null && properties.getFrom().contains("@")) {
            return properties.getFrom().trim();
        }
        return properties.getUsername().trim();
    }

    static boolean deliverable(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }
        String domain = email.substring(email.indexOf('@') + 1).trim().toLowerCase();
        return domain.contains(".") && !domain.endsWith(".local") && !domain.equals("example.com");
    }
}
