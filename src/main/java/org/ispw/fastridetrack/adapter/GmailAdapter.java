package org.ispw.fastridetrack.adapter;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.PasswordAuthentication;

import java.util.Properties;

public class GmailAdapter implements EmailService {

    private final String username;
    private final String appPassword;

    public GmailAdapter() {
        this.username = System.getenv("GMAIL_EMAIL");
        this.appPassword = System.getenv("GMAIL_APP_PASSWORD");

        if (username == null || appPassword == null) {
            throw new IllegalStateException("Variabili d'ambiente GMAIL_EMAIL o GMAIL_APP_PASSWORD mancanti.");
        }
    }

    @Override
    public void sendEmail(String to, String subject, String body) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, appPassword);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }
}

