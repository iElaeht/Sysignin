package app.utils;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailUtils {
    private static final Dotenv dotenv = Dotenv.load();

    public static boolean sendWelcomeEmail(String toEmail, String token) {
        // Configuraciones del servidor (ejemplo con Gmail)
        String host = "smtp.gmail.com";
        final String user = dotenv.get("EMAIL_USER");
        final String pass = dotenv.get("EMAIL_PASS");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Verifica tu cuenta - SystemSignin");
            message.setText("¡Bienvenido! Tu código de activación es: " + token);

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}