package app.services;

import app.dao.AuditDAO;
import app.dao.UserDAO;
import app.dao.SessionDAO;
import app.dao.NotificationDAO;
import app.models.User;
import app.models.UserSession;
import app.utils.TokenUtils;
import app.utils.ValidationUtils;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private UserDAO userDAO = new UserDAO();
    private AuditDAO auditDAO = new AuditDAO();
    private SessionDAO sessionDAO = new SessionDAO();
    private NotificationDAO notificationDAO = new NotificationDAO();
    public String registerUser(String username, String email, String password, String ip, String country, String city, String userAgent) {
        
        if (!ValidationUtils.isValidEmail(email)) {
            return "ERROR: Formato de correo inválido.";
        }
        
        if (userDAO.getUserByEmail(email) != null) {
            auditDAO.insertLog(null, email, "REGISTER_FAIL_DUPLICATE", ip, userAgent, "Correo ya registrado.");
            return "ERROR: El correo ya está en uso.";
        }

        User newUser = new User();
        newUser.setUuidUser(TokenUtils.generateUUID());
        newUser.setUsername(username);
        newUser.setPassword(password); // El DAO se encarga del Hash BCrypt
        newUser.setEmail(email);
        newUser.setRegistrationIp(ip);
        newUser.setCountry(country);
        newUser.setCity(city);
        newUser.setToken(TokenUtils.generateNumericToken());

        if (userDAO.registerUser(newUser)) {
            auditDAO.insertLog(null, email, "REGISTER_SUCCESS", ip, userAgent, "Usuario creado, esperando activación.");
            notificationDAO.createNotification(newUser.getUuidUser(), "¡Bienvenido!", "Verifica tu cuenta con el código enviado.", "info");
            
            // Simulación de envío de correo (DEBUG)
            System.out.println(">>> EMAIL ENVIADO A " + email + " CON TOKEN: " + newUser.getToken());
            
            return "SUCCESS: Registro exitoso. Revisa tu correo.";
        }
        
        return "ERROR: Error técnico en el servidor.";
    }
    public String verifyAccount(String email, String token, String ip, String userAgent) {
        boolean activated = userDAO.activateAccount(email, token);

        if (activated) {
            User user = userDAO.getUserByEmail(email);
            auditDAO.insertLog(user.getIdUser(), email, "ACCOUNT_ACTIVATED", ip, userAgent, "Cuenta activada con éxito.");
            notificationDAO.createNotification(user.getUuidUser(), "¡Cuenta Verificada!", "Has subido al Nivel 1 de seguridad.", "success");
            return "SUCCESS: Cuenta activada. Ya puedes iniciar sesión.";
        } else {
            auditDAO.insertLog(null, email, "ACTIVATION_FAIL", ip, userAgent, "Token incorrecto o expirado.");
            return "ERROR: Código inválido o vencido.";
        }
    }
    public User login(String email, String password, String ip, String userAgent, String country, String city, String deviceType) {
        // Buscar usuario
        User user = userDAO.getUserByEmail(email);
        
        if (user == null) {
            auditDAO.insertLog(null, email, "LOGIN_FAIL_NOT_FOUND", ip, userAgent, "Usuario no existe.");
            return null;
        }

        // Verificar contraseña
        if (BCrypt.checkpw(password, user.getPassword())) {
            
            // Crear objeto de sesión para el SessionDAO
            UserSession session = new UserSession();
            session.setUserUuid(user.getUuidUser());
            session.setDeviceInfo(userAgent);
            session.setDeviceType(deviceType);
            session.setIpAddress(ip);
            session.setCountry(country);
            session.setCity(city);
            
            sessionDAO.createSession(session);

            // Auditoría y Alerta
            auditDAO.insertLog(user.getIdUser(), email, "LOGIN_SUCCESS", ip, userAgent, "Sesión iniciada correctamente.");
            notificationDAO.createNotification(user.getUuidUser(), "Nuevo Inicio de Sesión", "Detectado desde " + city + ", " + country, "warning");

            return user;
        } else {
            auditDAO.insertLog(user.getIdUser(), email, "LOGIN_FAIL_PASSWORD", ip, userAgent, "Contraseña incorrecta.");
            return null;
        }
    }
}