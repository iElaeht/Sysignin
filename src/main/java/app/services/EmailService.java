package app.services;

import app.config.EnvConfig;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

/**
 * SERVICIO: EmailService
 * Descripción: Gestión de envíos de correo con plantillas HTML minimalistas.
 * Actualización: Diseño centrado y tokens en negrita para máxima visibilidad.
 */
public class EmailService {

    private final String emailUser;
    private final String emailPass;
    private final String appName = "Seguridad App";

    public EmailService() {
        this.emailUser = EnvConfig.get("EMAIL_USER");
        this.emailPass = EnvConfig.get("EMAIL_PASS");
    }

    // ==========================================
    // 1. LÓGICA DE ENVÍO (Multihilo)
    // ==========================================

    private void send(String to, String subject, String htmlBody) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailUser, emailPass);
            }
        });

        new Thread(() -> {
            try {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(emailUser, appName));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                
                message.setSubject(subject, "UTF-8");
                message.setContent(wrapInTemplate(htmlBody), "text/html; charset=UTF-8");
                
                Transport.send(message);
            } catch (Exception e) {
                System.err.println(">>> [EMAIL ERROR]: " + e.getMessage());
            }
        }).start();
    }

    // ==========================================
    // 2. TOKENS DE VALIDACIÓN
    // ==========================================

    public void sendActivationToken(String to, String token) {
        String body = "<h1>Activa tu cuenta</h1>" +
                      "<p>Gracias por unirte. Usa el siguiente código para completar tu registro:</p>" +
                      "<div class='token'>" + token + "</div>" +
                      "<p style='font-size:12px;'>Este código expirará en 15 minutos.</p>";
        send(to, "Código de activación - " + appName, body);
    }

    public void sendPasswordChangeToken(String to, String token) {
        String body = "<h1>Cambio de contraseña</h1>" +
                      "<p>Has solicitado cambiar tu clave de acceso. Tu código de verificación es:</p>" +
                      "<div class='token' style='color:#e74c3c;'>" + token + "</div>" +
                      "<p>Si no solicitaste esto, ignora este correo.</p>";
        send(to, "Código de seguridad - " + appName, body);
    }

    public void sendEmailChangeToken(String to, String token) {
        String body = "<h1>Confirmar nuevo correo</h1>" +
                      "<p>Usa este código para verificar tu nueva dirección de correo electrónico:</p>" +
                      "<div class='token' style='color:#f39c12;'>" + token + "</div>";
        send(to, "Verificación de correo - " + appName, body);
    }

    public void sendRecoveryToken(String to, String token) {
        String body = "<h1>Recuperación de acceso</h1>" +
                      "<p>Tu código temporal para restablecer el acceso a tu cuenta es:</p>" +
                      "<div class='token'>" + token + "</div>";
        send(to, "Recupera tu cuenta - " + appName, body);
    }

    // ==========================================
    // 3. ALERTAS DE SEGURIDAD
    // ==========================================

    public void sendLoginFailedAlert(String to) {
        String body = "<h1>Acceso bloqueado</h1>" +
                      "<p>Se han detectado <strong>5 intentos fallidos</strong> de inicio de sesión.</p>" +
                      "<p>Por seguridad, tu cuenta ha sido suspendida temporalmente por 10 minutos.</p>";
        send(to, "Alerta de seguridad - " + appName, body);
    }

    public void sendBruteForceAlert(String to) {
        String body = "<h1>Bloqueo preventivo</h1>" +
                      "<p>Detectamos actividad inusual con el uso de tokens en tu cuenta.</p>" +
                      "<p><strong>Acceso restringido por 15 minutos.</strong></p>";
        send(to, "Actividad sospechosa detectada - " + appName, body);
    }

    public void sendNewLoginAlert(String to, String ip, String city, String device) {
        String body = "<h1>Nuevo inicio de sesión</h1>" +
                      "<p>Se ha accedido a tu cuenta desde una nueva ubicación:</p>" +
                      "<div style='background:#f9f9f9; padding:15px; border-radius:10px; border:1px solid #eee; display:inline-block; text-align:left;'>" +
                      "• <strong>IP:</strong> " + ip + "<br>" +
                      "• <strong>Ciudad:</strong> " + city + "<br>" +
                      "• <strong>Dispositivo:</strong> " + device + "</div>";
        send(to, "Nuevo inicio de sesión - " + appName, body);
    }

    public void sendSecurityAlert(String to, String details) {
        String body = "<h1>Actividad reciente</h1>" +
                      "<p>Se realizó la siguiente acción en tu cuenta:</p>" +
                      "<p><strong>" + details + "</strong></p>";
        send(to, "Aviso de seguridad - " + appName, body);
    }

    // ==========================================
    // 4. NOTIFICACIONES DE ÉXITO
    // ==========================================

    public void sendPrimaryEmailChangedAlert(String oldEmail) {
        String body = "<h1>Correo actualizado</h1>" +
                      "<p>Tu dirección de correo principal ha sido cambiada exitosamente.</p>" +
                      "<p>Si no realizaste este cambio, contacta con soporte de inmediato.</p>";
        send(oldEmail, "Cambio de correo exitoso - " + appName, body);
    }

    public void sendRecoveryEmailUpdatedAlert(String primaryEmail) {
        String body = "<h1>Configuración actualizada</h1>" +
                      "<p>Tu correo de recuperación ha sido actualizado correctamente.</p>";
        send(primaryEmail, "Correo de recuperación actualizado - " + appName, body);
    }

    public void sendAccountDeletionNotice(String to) {
        String body = "<h1>Cuenta desactivada</h1>" +
                      "<p>Tu cuenta ha sido dada de baja de nuestro sistema exitosamente.</p>";
        send(to, "Adiós - " + appName, body);
    }

    // ==========================================
    // 5. DISEÑO (Template Centrado y Resaltado)
    // ==========================================

    private String wrapInTemplate(String content) {
        return "<!DOCTYPE html><html lang='es'>" +
               "<head><meta charset='UTF-8'></head>" +
               "<body style='font-family:-apple-system,BlinkMacSystemFont,\"Segoe UI\",Roboto,Helvetica,Arial,sans-serif; background-color:#ffffff; color:#333; margin:0; padding:40px; text-align:center;'>" +
               "<div style='max-width:450px; margin:0 auto; line-height:1.6;'>" +
               "<div style='margin-bottom:40px; font-weight:bold; font-size:18px; color:#000;'>" + appName + "</div>" +
               "<div style='font-size:15px;'>" + content + "</div>" +
               "<div style='margin-top:50px; padding-top:20px; border-top:1px solid #eee; font-size:12px; color:#999; text-align:center;'>" +
               "Este es un mensaje automático generado por nuestro sistema de seguridad. Por favor, no respondas a este correo." +
               "</div></div>" +
               "<style>" +
               ".token { " +
               "  background:#f4f4f7; " +
               "  padding:20px; " +
               "  text-align:center; " +
               "  font-size:32px; " +
               "  font-weight:800; " +
               "  letter-spacing:6px; " +
               "  border-radius:12px; " +
               "  margin:25px auto; " +
               "  color:#2d3748; " +
               "  width: fit-content; " +
               "  padding-left: 40px; " +
               "  padding-right: 40px; " +
               "  border: 1px solid #e2e8f0;" +
               "}" +
               "h1 { font-size:22px; margin-bottom:20px; color:#1a202c; }" +
               "</style></body></html>";
    }
}