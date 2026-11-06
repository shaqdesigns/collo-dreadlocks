package com.collo.dreadlocks.notification;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

final class SmtpClient {

    void send(
            String host,
            int port,
            String username,
            String password,
            String from,
            String to,
            String subject,
            String body) throws IOException {
        try (Socket plain = new Socket()) {
            plain.connect(new InetSocketAddress(host, port), 8000);
            plain.setSoTimeout(8000);
            Exchange exchange = port == 465
                    ? new Exchange(upgrade(plain, host, port))
                    : new Exchange(plain);
            exchange.expect(220);
            exchange.command("EHLO collo-dreadlocks", 250);
            if (port != 465) {
                exchange.command("STARTTLS", 220);
                exchange = new Exchange(upgrade(plain, host, port));
                exchange.command("EHLO collo-dreadlocks", 250);
            }
            exchange.command("AUTH LOGIN", 334);
            exchange.command(Base64.getEncoder().encodeToString(username.getBytes(StandardCharsets.UTF_8)), 334);
            exchange.command(Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8)), 235);
            exchange.command("MAIL FROM:<" + from + ">", 250);
            exchange.command("RCPT TO:<" + to + ">", 250);
            exchange.command("DATA", 354);
            exchange.write(message(from, to, subject, body));
            exchange.command(".", 250);
            exchange.command("QUIT", 221);
        }
    }

    private static SSLSocket upgrade(Socket plain, String host, int port) throws IOException {
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket tls = (SSLSocket) factory.createSocket(plain, host, port, true);
        tls.setSoTimeout(8000);
        tls.startHandshake();
        return tls;
    }

    private static String message(String from, String to, String subject, String body) {
        String safeSubject = subject.replace("\r", " ").replace("\n", " ");
        StringBuilder message = new StringBuilder();
        message.append("From: ").append(from).append("\r\n");
        message.append("To: ").append(to).append("\r\n");
        message.append("Subject: ").append(safeSubject).append("\r\n");
        message.append("MIME-Version: 1.0\r\n");
        message.append("Content-Type: text/plain; charset=UTF-8\r\n");
        message.append("Content-Transfer-Encoding: 8bit\r\n");
        message.append("\r\n");
        for (String line : body.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1)) {
            if (line.startsWith(".")) {
                message.append('.');
            }
            message.append(line).append("\r\n");
        }
        return message.toString();
    }

    private static final class Exchange {
        private final InputStream input;
        private final OutputStream output;

        private Exchange(Socket socket) throws IOException {
            this.input = socket.getInputStream();
            this.output = socket.getOutputStream();
        }

        private void command(String line, int expected) throws IOException {
            output.write((line + "\r\n").getBytes(StandardCharsets.UTF_8));
            output.flush();
            expect(expected);
        }

        private void write(String text) throws IOException {
            output.write(text.getBytes(StandardCharsets.UTF_8));
            output.flush();
        }

        private void expect(int expected) throws IOException {
            int code = readCode();
            if (code != expected) {
                throw new IOException("SMTP replied " + code + ", expected " + expected);
            }
        }

        private int readCode() throws IOException {
            String line = readLine();
            if (line.length() < 3) {
                throw new IOException("SMTP closed the connection");
            }
            int code = Integer.parseInt(line.substring(0, 3));
            while (line.length() >= 4 && line.charAt(3) == '-') {
                line = readLine();
            }
            return code;
        }

        private String readLine() throws IOException {
            StringBuilder line = new StringBuilder();
            int value;
            while ((value = input.read()) != -1) {
                if (value == '\n') {
                    break;
                }
                if (value != '\r') {
                    line.append((char) value);
                }
            }
            if (value == -1 && line.isEmpty()) {
                throw new IOException("SMTP closed the connection");
            }
            return line.toString();
        }
    }
}
