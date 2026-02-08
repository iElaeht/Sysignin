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
 * Conteo de Métodos: 13 (Mantenido)
 * Descripción: Motor de lógica de negocio para autenticación, seguridad y gestión de cuentas.
 */
public class AuthService {

    private UserDAO userDAO = new UserDAO();
    private AuditDAO auditDAO = new AuditDAO();
    private SessionDAO sessionDAO = new SessionDAO();
    private SecurityTokenDAO securityTokenDAO = new SecurityTokenDAO();
    private EmailService emailService = new EmailService();

    private static final int MAX_ATTEMPTS = 5;
    private static final int PENALTY_MINUTES = 15;
    private static final int COOLDOWN_SECONDS = 40;

    // ==========================================
    // 1. MÉTODOS DE APOYO E INTERNOS
    // ==========================================

    private boolean isUserPenalized(User user) {
        if (user == null || user.getPenaltyTime() == null) return false;
        return LocalDateTime.now().isBefore(user.getPenaltyTime());
    }

    // ==========================================
    // 2. REGISTRO Y ACTIVACIÓN
    // ==========================================

    public String registerUser(String username, String email, String password, String ip, String country, String city) {
        if (!ValidationUtils.isValidEmail(email)) return "ERROR: Correo inválido.";
        
        User existingUser = userDAO.getUserByIdentifier(email);
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

        // ACTUALIZACIÓN: Ahora capturamos el ID generado
        int generatedId = userDAO.registerUser(newUser);
        if (generatedId > 0) {
            String location = city + ", " + country;
            // ACTUALIZACIÓN: Log con ID real, Email y Location
            auditDAO.insertLog(generatedId, newUser.getUuidUser(), email, "ACCOUNT_CREATED", ip, location, "N/A", "Registro inicial exitoso.");
            emailService.sendActivationToken(email, token);
            return "SUCCESS"; 
        }
        return "ERROR: Fallo al guardar.";
    }
    
    public User getUserByEmail(String email) {
        return userDAO.getUserByIdentifier(email);
    }

    public String verifyAccount(String email, String token, String ip, String userAgent, boolean remember) {
        User user = userDAO.getUserByIdentifier(email);
        if (user == null) return "ERROR: Usuario no encontrado.";

        if (isUserPenalized(user)) return "ERROR: CUENTA_PENALIZADA"; 

        String location = user.getCity() + ", " + user.getCountry();

        // Intentar activar/verificar
        if (userDAO.activateAccount(email, token)) {
            
            // --- LÓGICA DE RECORDAR DISPOSITIVO ---
            if (remember) {
                // Actualizamos la IP de registro por la IP actual (así se vuelve "conocida")
                userDAO.updateRegistrationIp(email, ip);
                auditDAO.insertLog(user.getIdUser(), user.getUuidUser(), email, "DEVICE_REMEMBERED", ip, location, userAgent, "IP marcada como confiable");
            }

            // Log de éxito general
            auditDAO.insertLog(user.getIdUser(), user.getUuidUser(), email, "ACCOUNT_ACTIVATED", ip, location, userAgent, "Éxito");
            
            // Limpiamos intentos fallidos tras éxito
            userDAO.resetAttempts(email); 
            
            return "SUCCESS";
        } else {
            // Lógica de intentos fallidos (se mantiene igual)
            userDAO.incrementTokenAttempts(email);
            User updatedUser = userDAO.getUserByIdentifier(email);
            
            if (updatedUser.getTokenAttempts() >= MAX_ATTEMPTS) {
                userDAO.applyPenalty(email, 10);
                emailService.sendBruteForceAlert(email);
                return "ERROR: CUENTA_PENALIZADA"; 
            }
            return "ERROR: Código incorrecto. Intento " + updatedUser.getTokenAttempts() + " de 5.";
        }
    }

    public String resendToken(String email) {
        User user = userDAO.getUserByIdentifier(email);
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
    // 3. ACCESO Y SESIONES
    // ==========================================
        public User login(String identifier, String password, String ip, String userAgent, String deviceType, String city) {
        User user = userDAO.getUserByIdentifier(identifier); 
        if (user == null) {
            auditDAO.insertLog(null, identifier, null, "LOGIN_FAILED_UNKNOWN", ip, city, userAgent, "Usuario inexistente");
            return null;
        }

        String location = city + ", " + user.getCountry();

        if (isUserPenalized(user)) {
            auditDAO.insertLog(user.getIdUser(), user.getUuidUser(), user.getEmail(), "LOGIN_ATTEMPT_PENALIZED", ip, location, userAgent, "Intento durante bloqueo");
            return null;
        }

        if (BCrypt.checkpw(password, user.getPassword())) {
            if (!"Active".equalsIgnoreCase(user.getState())) {
                auditDAO.insertLog(user.getIdUser(), user.getUuidUser(), user.getEmail(), "LOGIN_FAILED_INACTIVE", ip, location, userAgent, "Cuenta no activada");
                return null;
            }

            // --- LÓGICA DE NUEVA IP / 2FA ---
            if (user.getRegistrationIp() != null && !user.getRegistrationIp().equals(ip)) {
                String securityToken = TokenUtils.generateAlphanumericToken(9);
                userDAO.updateToken(user.getEmail(), securityToken);
                emailService.sendNewLoginAlert(user.getEmail(), ip, city, deviceType); 
                
                auditDAO.insertLog(user.getIdUser(), user.getUuidUser(), user.getEmail(), "LOGIN_REQUIRE_2FA", ip, location, userAgent, "IP nueva detectada");
                
                // Creamos un objeto "fantasma" o lanzamos una señal al Servlet
                User trigger2FA = new User();
                trigger2FA.setEmail(user.getEmail());
                trigger2FA.setState("NEED_VERIFICATION");
                return trigger2FA; 
            }

            // --- LOGIN EXITOSO ---
            UserSession session = new UserSession();
            session.setUserUuid(user.getUuidUser());
            session.setDeviceInfo(userAgent);
            session.setDeviceType(deviceType);
            session.setIpAddress(ip);
            sessionDAO.createSession(session);

            userDAO.resetAttempts(user.getEmail());
            userDAO.updateLastIp(user.getUuidUser(), ip); 
            auditDAO.insertLog(user.getIdUser(), user.getUuidUser(), user.getEmail(), "LOGIN_SUCCESS", ip, location, userAgent, "OK");
            return user;

        } else {
            userDAO.incrementLoginAttempts(user.getEmail());
            int currentAttempts = user.getLoginAttempts() + 1;
            if (currentAttempts >= MAX_ATTEMPTS) {
                userDAO.applyPenalty(user.getEmail(), PENALTY_MINUTES);
                emailService.sendLoginFailedAlert(user.getEmail());
                auditDAO.insertLog(user.getIdUser(), user.getUuidUser(), user.getEmail(), "LOGIN_BLOCKED", ip, location, userAgent, "Bloqueo por " + PENALTY_MINUTES + " min");
            } else {
                auditDAO.insertLog(user.getIdUser(), user.getUuidUser(), user.getEmail(), "LOGIN_FAILED", ip, location, userAgent, "Intento fallido: " + currentAttempts);
            }
            return null;
        }
    }

    public boolean logout(int idSession, String userUuid, String ip, String userAgent) {
        if (sessionDAO.terminateSession(idSession, userUuid)) {
            User user = userDAO.getUserByUuid(userUuid);
            String email = (user != null) ? user.getEmail() : null;
            String location = (user != null) ? user.getCity() + ", " + user.getCountry() : "Unknown";

            auditDAO.insertLog(null, userUuid, email, "LOGOUT", ip, location, userAgent, "Sesión cerrada.");
            return true;
        }
        return false;
    }

    public String resetUserAttempts(String email) {
        User user = userDAO.getUserByIdentifier(email);
        if (user == null) return "ERROR: Usuario no encontrado.";
        
        userDAO.resetAttempts(email);
        String location = user.getCity() + ", " + user.getCountry();
        auditDAO.insertLog(user.getIdUser(), user.getUuidUser(), email, "PENALTY_CLEARED", "SYSTEM", location, "Web", "Limpieza de intentos.");
        return "SUCCESS";
    }

    // ==========================================
    // 4. RECUPERACIÓN Y CAMBIO PWD
    // ==========================================

    public String recoverPasswordRequest(String email) {
        User user = userDAO.getUserByIdentifier(email);
        if (user == null) return "SUCCESS"; 

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
                String location = user.getCity() + ", " + user.getCountry();
                emailService.sendSecurityAlert(user.getEmail(), "Contraseña cambiada desde IP: " + ip);
                auditDAO.insertLog(user.getIdUser(), uuid, user.getEmail(), "PASSWORD_CHANGED", ip, location, userAgent, "Éxito");
            }
            return "SUCCESS";
        }
        return "ERROR: Fallo actualización.";
    }

    // ==========================================
    // 5. GESTIÓN DE CORREO (2 métodos)
    // ==========================================

    public String requestEmailChange(String uuid, String newEmail) {
        if (!ValidationUtils.isValidEmail(newEmail)) return "ERROR: Correo inválido.";
        if (userDAO.getUserByIdentifier(newEmail) != null) return "ERROR: El correo ya está en uso.";

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
            
            User user = userDAO.getUserByUuid(uuid);
            String location = (user != null) ? user.getCity() + ", " + user.getCountry() : "Unknown";
            
            auditDAO.insertLog(user != null ? user.getIdUser() : null, uuid, newEmail, "EMAIL_CHANGED", ip, location, userAgent, "De: " + oldEmail + " a: " + newEmail);
            return "SUCCESS";
        }
        return "ERROR: No se pudo actualizar el correo.";
    }
}