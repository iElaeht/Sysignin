package app.services;

import app.dao.*;
import app.models.*;
import app.utils.TokenUtils;
import app.utils.ValidationUtils;
import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * SERVICIO: AuthService
 * Conteo de Métodos: 13
 * Descripción: Motor de lógica de negocio para autenticación, seguridad y gestión de cuentas.
 */
public class AuthService {

    // DAOs y Servicios Externos
    private UserDAO userDAO = new UserDAO();
    private AuditDAO auditDAO = new AuditDAO();
    private SessionDAO sessionDAO = new SessionDAO();
    private SecurityTokenDAO securityTokenDAO = new SecurityTokenDAO();
    private EmailService emailService = new EmailService();

    // Constantes de Configuración
    private static final int MAX_ATTEMPTS = 5;
    private static final int PENALTY_MINUTES = 15;
    private static final int COOLDOWN_SECONDS = 30;

    // ==========================================
    // 1. MÉTODOS DE APOYO E INTERNOS (1 método)
    // ==========================================

    private boolean isUserPenalized(User user) {
        if (user == null || user.getPenaltyTime() == null) return false;
        return LocalDateTime.now().isBefore(user.getPenaltyTime());
    }

    // ==========================================
    // 2. REGISTRO Y ACTIVACIÓN (3 métodos)
    // ==========================================

    public String registerUser(String username, String email, String password, String ip, String country, String city) {
        if (!ValidationUtils.isValidEmail(email)) return "ERROR: Correo inválido.";
        
        User existingUser = userDAO.getUserByEmail(email);
        if (existingUser != null) {
            if ("Active".equalsIgnoreCase(existingUser.getState())) return "ERROR: El correo ya existe.";
            if (isUserPenalized(existingUser)) return "ERROR: Penalizado. Espera 15 min.";

            long secondsSinceReg = Duration.between(existingUser.getDateRegistration(), LocalDateTime.now()).getSeconds();
            if (secondsSinceReg < COOLDOWN_SECONDS) return "ERROR: Espera 30s.";

            String newToken = TokenUtils.generateAlphanumericToken(9);
            if (userDAO.updateToken(email, newToken)) {
                emailService.sendActivationToken(email, newToken);
                return "SUCCESS"; 
            }
            return "ERROR: Fallo al actualizar.";
        }

        User newUser = new User();
        newUser.setUuidUser(TokenUtils.generateUUID());
        newUser.setUsername(username); 
        newUser.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(12)));
        newUser.setEmail(email);
        newUser.setRegistrationIp(ip);
        newUser.setCountry(country);
        newUser.setCity(city);
        
        String token = TokenUtils.generateAlphanumericToken(9); 
        newUser.setToken(token);

        if (userDAO.registerUser(newUser)) {
            auditDAO.insertLog(null, email, "ACCOUNT_CREATED", ip, "N/A", "Registro inicial exitoso.");
            emailService.sendActivationToken(email, token);
            return "SUCCESS"; 
        }
        return "ERROR: Fallo al guardar.";
    }

    public String verifyAccount(String email, String token, String ip, String userAgent) {
        User user = userDAO.getUserByEmail(email);
        if (user == null) return "ERROR: Usuario no encontrado.";

        if (isUserPenalized(user)) return "ERROR: CUENTA_PENALIZADA"; 

        if (userDAO.activateAccount(email, token)) {
            auditDAO.insertLog(user.getIdUser(), email, "ACCOUNT_ACTIVATED", ip, userAgent, "Éxito");
            return "SUCCESS";
        } else {
            userDAO.incrementTokenAttempts(email);
            User updatedUser = userDAO.getUserByEmail(email);
            
            if (updatedUser.getTokenAttempts() >= MAX_ATTEMPTS) {
                userDAO.applyPenalty(email, 10); // Penalización corta por fallos de token
                emailService.sendBruteForceAlert(email);
                return "ERROR: CUENTA_PENALIZADA"; 
            }
            return "ERROR: Código incorrecto. Intento " + updatedUser.getTokenAttempts() + " de 5.";
        }
    }

    public String resendToken(String email) {
        User user = userDAO.getUserByEmail(email);
        if (user == null) return "ERROR: Usuario no encontrado.";

        if (isUserPenalized(user)) return "ERROR: Cuenta penalizada.";
        
        String newToken = TokenUtils.generateAlphanumericToken(9);
        if (userDAO.updateToken(email, newToken)) {
            emailService.sendActivationToken(email, newToken);
            return "SUCCESS";
        }
        return "ERROR: No se pudo generar el código.";
    }

    // ==========================================
    // 3. ACCESO Y SESIONES (3 métodos)
    // ==========================================

    public User login(String identifier, String password, String ip, String userAgent, String deviceType, String city) {
        User user = userDAO.getUserByEmail(identifier); 
        if (user != null && isUserPenalized(user)) return null;

        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            if (!"Active".equalsIgnoreCase(user.getState())) return null; 

            // Alerta si la IP ha cambiado desde el registro
            if (user.getRegistrationIp() != null && !user.getRegistrationIp().equals(ip)) {
                emailService.sendNewLoginAlert(user.getEmail(), ip, city, deviceType);
            }

            UserSession session = new UserSession();
            session.setUserUuid(user.getUuidUser());
            session.setDeviceInfo(userAgent);
            session.setDeviceType(deviceType);
            session.setIpAddress(ip);
            sessionDAO.createSession(session);
            
            userDAO.resetAttempts(user.getEmail());
            userDAO.updateLastIp(user.getUuidUser(), ip); // Actualizamos IP y LastLogin
            auditDAO.insertLog(user.getIdUser(), user.getEmail(), "LOGIN_SUCCESS", ip, userAgent, "OK");
            return user;
        } else {
            if (user != null) {
                userDAO.incrementLoginAttempts(user.getEmail());
                int attempts = user.getLoginAttempts() + 1;

                if (attempts >= MAX_ATTEMPTS) {
                    userDAO.applyPenalty(user.getEmail(), PENALTY_MINUTES);
                    emailService.sendLoginFailedAlert(user.getEmail());
                    auditDAO.insertLog(user.getIdUser(), user.getEmail(), "LOGIN_BLOCKED", ip, userAgent, "Bloqueo 15 min");
                } else {
                    auditDAO.insertLog(user.getIdUser(), user.getEmail(), "LOGIN_FAILED", ip, userAgent, "Intento: " + attempts);
                }
            } else {
                auditDAO.insertLog(null, identifier, "LOGIN_FAILED_UNKNOWN", ip, userAgent, "Usuario inexistente");
            }
            return null;
        }
    }

    public boolean logout(int idSession, String userUuid, String ip, String userAgent) {
        if (sessionDAO.terminateSession(idSession, userUuid)) {
            auditDAO.insertLog(null, userUuid, "LOGOUT", ip, userAgent, "Sesión cerrada.");
            return true;
        }
        return false;
    }

    public String resetUserAttempts(String email) {
        User user = userDAO.getUserByEmail(email);
        if (user == null) return "ERROR: Usuario no encontrado.";
        
        userDAO.resetAttempts(email);
        auditDAO.insertLog(user.getIdUser(), email, "PENALTY_CLEARED", "SYSTEM", "Web", "Limpieza de intentos.");
        return "SUCCESS";
    }

    // ==========================================
    // 4. RECUPERACIÓN Y CAMBIO PWD (3 métodos)
    // ==========================================

    public String recoverPasswordRequest(String email) {
        User user = userDAO.getUserByEmail(email);
        if (user == null) return "SUCCESS"; // Seguridad: No confirmar si el email existe

        SecurityToken lastToken = securityTokenDAO.getLastToken(user.getUuidUser(), "PWD_RECOVERY");
        if (lastToken != null && Duration.between(lastToken.getCreatedAt(), LocalDateTime.now()).getSeconds() < COOLDOWN_SECONDS) {
            return "ERROR: Reintento rápido.";
        }

        String recoveryCode = TokenUtils.generateNumericToken();
        SecurityToken st = new SecurityToken();
        st.setUserUuid(user.getUuidUser());
        st.setTokenType("PWD_RECOVERY");
        st.setTokenCode(recoveryCode);
        st.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        if (securityTokenDAO.insertToken(st)) {
            emailService.sendRecoveryToken(email, recoveryCode);
        }
        return "SUCCESS";
    }

    public String requestPasswordChange(String uuid, String currentPassword) {
        User user = userDAO.getUserByUuid(uuid);
        if (user == null || !BCrypt.checkpw(currentPassword, user.getPassword())) return "ERROR: Credenciales inválidas.";

        SecurityToken lastToken = securityTokenDAO.getLastToken(uuid, "PWD_CHANGE");
        if (lastToken != null && Duration.between(lastToken.getCreatedAt(), LocalDateTime.now()).getSeconds() < COOLDOWN_SECONDS) {
            return "ERROR: Reintento rápido.";
        }

        String code = TokenUtils.generateNumericToken();
        SecurityToken st = new SecurityToken();
        st.setUserUuid(uuid);
        st.setTokenType("PWD_CHANGE");
        st.setTokenCode(code);
        st.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        if (securityTokenDAO.insertToken(st)) {
            emailService.sendPasswordChangeToken(user.getEmail(), code);
            return "SUCCESS";
        }
        return "ERROR: Fallo generación.";
    }

    public String confirmPasswordChange(String uuid, String code, String newPassword, String ip, String userAgent) {
        SecurityToken st = securityTokenDAO.validateToken(uuid, code, "PWD_CHANGE");
        if (st == null) return "ERROR: Código inválido.";

        String hashedNewPwd = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        if (userDAO.updatePassword(uuid, hashedNewPwd)) {
            securityTokenDAO.markAsUsed(st.getIdToken());
            User user = userDAO.getUserByUuid(uuid);
            if (user != null) {
                emailService.sendSecurityAlert(user.getEmail(), "Contraseña cambiada desde IP: " + ip);
                auditDAO.insertLog(user.getIdUser(), uuid, "PASSWORD_CHANGED", ip, userAgent, "Éxito");
            }
            return "SUCCESS";
        }
        return "ERROR: Fallo actualización.";
    }

    // ==========================================
    // 5. GESTIÓN DE CORREO (3 métodos)
    // ==========================================

    public String requestEmailChange(String uuid, String newEmail) {
        if (!ValidationUtils.isValidEmail(newEmail)) return "ERROR: Correo inválido.";
        if (userDAO.getUserByEmail(newEmail) != null) return "ERROR: El correo ya está en uso.";

        String code = TokenUtils.generateNumericToken();
        SecurityToken st = new SecurityToken();
        st.setUserUuid(uuid);
        st.setTokenType("EMAIL_CHANGE");
        st.setTokenCode(code);
        st.setNewValue(newEmail); 
        st.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        if (securityTokenDAO.insertToken(st)) {
            emailService.sendEmailChangeToken(newEmail, code);
            return "SUCCESS";
        }
        return "ERROR: Fallo al generar código.";
    }

    public String confirmEmailChange(String uuid, String code, String oldEmail, String ip, String userAgent) {
        SecurityToken st = securityTokenDAO.validateToken(uuid, code, "EMAIL_CHANGE");
        if (st == null) return "ERROR: Código inválido o expirado.";

        String newEmail = st.getNewValue(); 
        if (userDAO.updateEmail(uuid, newEmail)) {
            securityTokenDAO.markAsUsed(st.getIdToken());
            emailService.sendPrimaryEmailChangedAlert(oldEmail); 
            auditDAO.insertLog(null, uuid, "EMAIL_CHANGED", ip, userAgent, "De: " + oldEmail + " a: " + newEmail);
            return "SUCCESS";
        }
        return "ERROR: No se pudo actualizar el correo.";
    }
}