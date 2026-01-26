package app.services;

import app.config.EnvConfig;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private final String host;
    private final String port;
    private final String emailUser;
    private final String emailPass;

    public EmailService() {
        this.host = "smtp.gmail.com";
        this.port = "587";
        this.emailUser = EnvConfig.get("EMAIL_USER");
        this.emailPass = EnvConfig.get("EMAIL_PASS");
    }

    public void sendHTMLEmail(String to, String subject, String htmlBody) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailUser, emailPass);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailUser));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(htmlBody, "text/html; charset=utf-8");

            // Lo enviamos en un hilo separado para no bloquear al usuario
            new Thread(() -> {
                try {
                    Transport.send(message);
                } catch (MessagingException e) {
                    System.err.println("Error enviando email: " + e.getMessage());
                }
            }).start();

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}