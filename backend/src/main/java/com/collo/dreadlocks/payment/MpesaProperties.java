package com.collo.dreadlocks.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mpesa")
public class MpesaProperties {

    private String environment = "sandbox";
    private String consumerKey = "";
    private String consumerSecret = "";
    private String passkey = "";
    private String shortcode = "174379";
    private String callbackUrl = "";
    private String transactionType = "CustomerPayBillOnline";

    public boolean configured() {
        return !blank(consumerKey)
                && !blank(consumerSecret)
                && !blank(passkey)
                && !blank(shortcode)
                && callbackUrl != null
                && callbackUrl.startsWith("https://");
    }

    public String baseUrl() {
        if ("production".equalsIgnoreCase(environment)) {
            return "https://api.safaricom.co.ke";
        }
        return "https://sandbox.safaricom.co.ke";
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public String getPasskey() {
        return passkey;
    }

    public void setPasskey(String passkey) {
        this.passkey = passkey;
    }

    public String getShortcode() {
        return shortcode == null || shortcode.isBlank() ? "174379" : shortcode.trim();
    }

    public void setShortcode(String shortcode) {
        this.shortcode = shortcode;
    }

    public String getCallbackUrl() {
        return callbackUrl == null ? "" : callbackUrl.trim();
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getTransactionType() {
        return transactionType == null || transactionType.isBlank()
                ? "CustomerPayBillOnline"
                : transactionType.trim();
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
