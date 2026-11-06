package com.collo.dreadlocks.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {

    private String host = "smtp-relay.brevo.com";
    private int port = 587;
    private String username = "";
    private String password = "";
    private String from = "";

    public String getHost() {
        return host == null || host.isBlank() ? "smtp-relay.brevo.com" : host.trim();
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port <= 0 ? 587 : port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
