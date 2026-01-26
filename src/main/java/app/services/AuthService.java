package app.services;

import app.dao.*;
import app.models.*;
import app.utils.TokenUtils;
import app.utils.ValidationUtils;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.Timestamp;

public class AuthService {

    private UserDAO userDAO = new UserDAO();
    private AuditDAO auditDAO = new AuditDAO();
    private SessionDAO sessionDAO = new SessionDAO();
    private NotificationDAO notificationDAO = new NotificationDAO();
    private SecurityTokenDAO securityTokenDAO = new SecurityTokenDAO();
    // Inyectamos el servicio de correo
    private EmailService emailService = new EmailService();

    // ==========================================
    // 1. REGISTRO, LOGIN Y ACTIVACIÓN
    // ==========================================

    public String registerUser(String username, String email, String password, String ip, String country, String city, String userAgent) {
        if (!ValidationUtils.isValidEmail(email)) return "ERROR: Correo inválido.";
        
        if (userDAO.getUserByEmail(email) != null) {
            auditDAO.insertLog(null, email, "REGISTER_FAIL", ip, userAgent, "Correo duplicado.");
            return "ERROR: El correo ya existe.";
        }

        User newUser = new User();
        newUser.setUuidUser(TokenUtils.generateUUID());
        newUser.setUsername(username);
        newUser.setPassword(password); 
        newUser.setEmail(email);
        newUser.setRegistrationIp(ip);
        newUser.setCountry(country);
        newUser.setCity(city);
        newUser.setToken(TokenUtils.generateNumericToken());

        if (userDAO.registerUser(newUser)) {
            auditDAO.insertLog(null, email, "REGISTER_SUCCESS", ip, userAgent, "Usuario registrado.");
            
            // ENVÍO REAL: Activación de cuenta
            String cuerpo = "<h2>Bienvenido, " + username + "</h2>" +
                            "<p>Usa el siguiente código para activar tu cuenta:</p>" +
                            "<h3>" + newUser.getToken() + "</h3>";
            emailService.sendHTMLEmail(email, "Activa tu cuenta - SystemSignin", cuerpo);
            
            return "SUCCESS: Revisa tu correo para activar tu cuenta.";
        }
        return "ERROR: No se pudo completar el registro.";
    }

    public User login(String email, String password, String ip, String userAgent, String country, String city, String deviceType) {
        User user = userDAO.getUserByEmail(email);
        
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            if (!"Active".equalsIgnoreCase(user.getState())) {
                auditDAO.insertLog(user.getIdUser(), email, "LOGIN_BLOCKED", ip, userAgent, "Cuenta inactiva/baneada.");
                return null; 
            }

            UserSession session = new UserSession();
            session.setUserUuid(user.getUuidUser());
            session.setDeviceInfo(userAgent);
            session.setDeviceType(deviceType);
            session.setIpAddress(ip);
            session.setCountry(country);
            session.setCity(city);
            sessionDAO.createSession(session);

            auditDAO.insertLog(user.getIdUser(), email, "LOGIN_SUCCESS", ip, userAgent, "Sesión iniciada.");
            notificationDAO.createNotification(user.getUuidUser(), "Nuevo Inicio de Sesión", "Acceso desde " + city, "Security");
            
            return user;
        }
        
        auditDAO.insertLog(null, email, "LOGIN_FAIL", ip, userAgent, "Credenciales incorrectas.");
        return null;
    }

    public String verifyAccount(String email, String token, String ip, String userAgent) {
        if (userDAO.activateAccount(email, token)) {
            User user = userDAO.getUserByEmail(email);
            if (user != null) {
                auditDAO.insertLog(user.getIdUser(), email, "ACCOUNT_VERIFIED", ip, userAgent, "Activación exitosa.");
                notificationDAO.createNotification(user.getUuidUser(), "¡Cuenta Activada!", "Ya tienes acceso total.", "System");
                return "SUCCESS: Cuenta activada. Ya puedes iniciar sesión.";
            }
        }
        auditDAO.insertLog(null, email, "VERIFY_FAIL", ip, userAgent, "Token inválido.");
        return "ERROR: El código es incorrecto o ha expirado.";
    }

    // ==========================================
    // 2. GESTIÓN DE SEGURIDAD: CAMBIO DE EMAIL
    // ==========================================

    public String requestEmailChange(String uuid, String currentPassword, String newEmail, String ip, String userAgent) {
        User user = userDAO.getUserByUuid(uuid);
        if (user == null || !BCrypt.checkpw(currentPassword, user.getPassword())) {
            auditDAO.insertLog(null, uuid, "EMAIL_CHANGE_REQ_FAIL", ip, userAgent, "Clave incorrecta.");
            return "ERROR: La contraseña actual es incorrecta.";
        }

        if (userDAO.getUserByEmail(newEmail) != null) return "ERROR: El nuevo correo ya está registrado.";

        String code = TokenUtils.generateNumericToken();
        SecurityToken st = new SecurityToken();
        st.setUserUuid(uuid);
        st.setTokenType("EMAIL_CHANGE");
        st.setTokenCode(code);
        st.setNewValue(newEmail);
        st.setExpiresAt(new Timestamp(System.currentTimeMillis() + (30 * 60 * 1000)));

        if (securityTokenDAO.insertToken(st)) {
            // ENVÍO REAL: Confirmación de nuevo email
            String cuerpo = "<h2>Confirmación de cambio de correo</h2>" +
                            "<p>Código de seguridad: <b>" + code + "</b></p>" +
                            "<p>Este código expira en 30 minutos.</p>";
            emailService.sendHTMLEmail(newEmail, "Código de Seguridad - Cambio de Email", cuerpo);
            
            return "SUCCESS: Código enviado a tu nuevo correo.";
        }
        return "ERROR: No se pudo procesar.";
    }

    public String confirmEmailChange(String uuid, String code, String ip, String userAgent) {
        SecurityToken st = securityTokenDAO.validateToken(uuid, code, "EMAIL_CHANGE");
        if (st == null) return "ERROR: Código inválido o expirado.";

        if (userDAO.updateEmail(uuid, st.getNewValue())) {
            securityTokenDAO.markAsUsed(st.getIdToken());
            auditDAO.insertLog(null, uuid, "EMAIL_UPDATE_SUCCESS", ip, userAgent, "Correo cambiado.");
            notificationDAO.createNotification(uuid, "Seguridad", "Tu correo principal ha sido actualizado.", "Alert");
            return "SUCCESS: Tu correo ha sido actualizado.";
        }
        return "ERROR: Error técnico al actualizar.";
    }

    // ==========================================
    // 3. GESTIÓN DE SEGURIDAD: CONTRASEÑA
    // ==========================================

    public String requestPasswordChange(String uuid, String currentPassword, String ip, String userAgent) {
        User user = userDAO.getUserByUuid(uuid);
        if (user == null || !BCrypt.checkpw(currentPassword, user.getPassword())) {
            return "ERROR: Contraseña actual incorrecta.";
        }

        String code = TokenUtils.generateNumericToken();
        SecurityToken st = new SecurityToken();
        st.setUserUuid(uuid);
        st.setTokenType("PWD_CHANGE");
        st.setTokenCode(code);
        st.setExpiresAt(new Timestamp(System.currentTimeMillis() + (15 * 60 * 1000)));

        if (securityTokenDAO.insertToken(st)) {
            // ENVÍO REAL: Cambio de contraseña
            String cuerpo = "<h2>Solicitud de cambio de contraseña</h2>" +
                            "<p>Tu código de verificación es: <b>" + code + "</b></p>";
            emailService.sendHTMLEmail(user.getEmail(), "Código de Seguridad - Contraseña", cuerpo);
            
            return "SUCCESS: Código enviado a tu correo.";
        }
        return "ERROR: Intenta más tarde.";
    }

    public String confirmPasswordChange(String uuid, String code, String newPassword, String ip, String userAgent) {
        SecurityToken st = securityTokenDAO.validateToken(uuid, code, "PWD_CHANGE");
        if (st == null) return "ERROR: Código inválido o expirado.";

        if (userDAO.updatePassword(uuid, newPassword)) {
            securityTokenDAO.markAsUsed(st.getIdToken());
            auditDAO.insertLog(null, uuid, "PWD_CHANGE_SUCCESS", ip, userAgent, "Clave cambiada.");
            notificationDAO.createNotification(uuid, "Seguridad", "Tu contraseña ha sido actualizada.", "Alert");
            return "SUCCESS: Contraseña actualizada correctamente.";
        }
        return "ERROR: No se pudo procesar el cambio.";
    }
}