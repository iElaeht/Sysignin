package app.controllers;

import app.models.User;
import app.services.AuthService;
import app.utils.NetUtils;
import app.utils.ValidationUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

import org.json.JSONObject;

@WebServlet("/auth/*")
public class AuthServlet extends HttpServlet {

    private AuthService authService = new AuthService();

    // ==========================================
    // 1. MÉTODOS DE CICLO DE VIDA (GET / POST)
    // ==========================================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        String action = (pathInfo == null || pathInfo.equals("/")) ? "" : pathInfo.substring(1);

        switch (action) {
            case "loginView":
                request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
                break;
            case "registerView":
                request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
                break;
            case "logout":
                handleLogout(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/index.jsp");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        String action = (pathInfo == null || pathInfo.equals("/")) ? "" : pathInfo.substring(1);
        
        String ip = NetUtils.getClientIp(request);
        String userAgent = NetUtils.getUserAgent(request);

        switch (action) {
            case "login":
                handleLogin(request, response, ip, userAgent);
                break;
            case "register":
                handleRegister(request, response, ip);
                break;
            case "verify":
                handleVerify(request, response, ip, userAgent);
                break;
            case "reset-attempts":
                handleResetAttempts(request, response);
                break;
            case "resend-token":
                handleResendToken(request, response);
                break;
            case "request-password-change":
                handleRequestPasswordChange(request, response);
                break;
            case "confirm-password-change":
                handleConfirmPasswordChange(request, response, ip, userAgent);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    // ==========================================
    // 2. GESTIÓN DE AUTENTICACIÓN (LOGIN/LOGOUT)
    // ==========================================

    private void handleLogin(HttpServletRequest request, HttpServletResponse response, String ip, String userAgent) 
                throws IOException {
        String identifier = request.getParameter("identifier"); 
        String password = request.getParameter("password");

        // Datos para el log de auditoría
        String city = "Sunampe"; 
        String deviceType = NetUtils.getDeviceType(request);

        // Llamada al servicio (que ahora puede retornar el flag NEED_VERIFICATION)
        User user = authService.login(identifier, password, ip, userAgent, deviceType, city);

        JSONObject jsonRes = new JSONObject();
        response.setContentType("application/json;charset=UTF-8");

        if (user != null) {
            // CASO A: El usuario necesita validar token por IP nueva
            if ("NEED_VERIFICATION".equals(user.getState())) {
                jsonRes.put("status", "needs_verification");
                jsonRes.put("email", user.getEmail());
                jsonRes.put("message", "Se ha detectado un inicio de sesión desde una ubicación nueva.");
            } 
            // CASO B: Login exitoso normal
            else {
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user); 
                session.setAttribute("idSession", user.getIdUser()); 
                
                jsonRes.put("status", "success");
                jsonRes.put("message", "¡Bienvenido, " + user.getUsername() + "!");
                jsonRes.put("redirect", request.getContextPath() + "/dashboard");
            }
        } else {
            // CASO C: Credenciales incorrectas, usuario bloqueado o inexistente
            jsonRes.put("status", "error");
            jsonRes.put("message", "Credenciales incorrectas o cuenta aún no activada.");
        }
        
        response.getWriter().write(jsonRes.toString());
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            Integer idSession = (Integer) session.getAttribute("idSession");
            if (user != null && idSession != null) {
                authService.logout(idSession, user.getUuidUser(), NetUtils.getClientIp(request), NetUtils.getUserAgent(request));
            }
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/index.jsp");
    }

    // ==========================================
    // 3. GESTIÓN DE REGISTRO Y VERIFICACIÓN
    // ==========================================

    private void handleRegister(HttpServletRequest request, HttpServletResponse response, String ip) 
            throws IOException {
        
        String rawUser = request.getParameter("username");
        String rawEmail = request.getParameter("email");
        String password = request.getParameter("password");

        String cleanEmail = ValidationUtils.normalizeEmail(rawEmail);
        String cleanUser = (rawUser != null) ? rawUser.trim().replaceAll("\\s+", "") : "";

        JSONObject jsonRes = new JSONObject();
        response.setContentType("application/json;charset=UTF-8");

        // Sincronización: Agregamos "Peru" y "Sunampe" (o captúralos de los parámetros si los tienes)
        String result = authService.registerUser(cleanUser, cleanEmail, password, ip, "Peru", "Sunampe");

        if ("SUCCESS".equals(result)) {
            jsonRes.put("status", "success");
            jsonRes.put("message", "Registro exitoso. Revisa tu correo.");
        } else {
            jsonRes.put("status", "error");
            jsonRes.put("message", result);
        }
        response.getWriter().write(jsonRes.toString());
    }
private void handleVerify(HttpServletRequest request, HttpServletResponse response, String ip, String userAgent) 
            throws IOException { 
    String email = request.getParameter("email");
    String token = request.getParameter("token");
    // Capturamos el booleano del checkbox "Recordar por 30 días"
    boolean remember = Boolean.parseBoolean(request.getParameter("remember"));
    
    // 1. Enviamos el nuevo parámetro 'remember' al servicio
    String result = authService.verifyAccount(email, token, ip, userAgent, remember);

    JSONObject jsonRes = new JSONObject();
    response.setContentType("application/json;charset=UTF-8");

    if ("SUCCESS".equals(result)) {
        User user = authService.getUserByEmail(email); 
        
        if (user != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user); 
            session.setAttribute("idSession", user.getIdUser());
            
            jsonRes.put("status", "success");
            jsonRes.put("redirect", request.getContextPath() + "/dashboard");
            jsonRes.put("message", "Verificación exitosa. ¡Bienvenido!");
        } else {
            jsonRes.put("status", "error");
            jsonRes.put("message", "Error al recuperar datos de sesión.");
        }
    } else if (result != null && result.contains("PENALIZADA")) {
        jsonRes.put("status", "penalty"); 
        jsonRes.put("message", "Bloqueado por seguridad. Intenta en unos minutos.");
    } else {
        jsonRes.put("status", "error");
        jsonRes.put("message", result != null ? result : "Código inválido");
    }
    
    response.getWriter().write(jsonRes.toString());
}

    private void handleResendToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String cleanEmail = ValidationUtils.normalizeEmail(request.getParameter("email"));
        JSONObject jsonRes = new JSONObject();
        response.setContentType("application/json");

        if (cleanEmail == null || cleanEmail.isEmpty()) {
            jsonRes.put("status", "error");
            jsonRes.put("message", "Email requerido.");
        } else {
            String result = authService.resendToken(cleanEmail);
            if ("SUCCESS".equals(result)) {
                jsonRes.put("status", "success");
                jsonRes.put("message", "Nuevo código enviado.");
            } else {
                jsonRes.put("status", "error");
                jsonRes.put("message", result);
            }
        }
        response.getWriter().write(jsonRes.toString());
    }

    private void handleResetAttempts(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String cleanEmail = ValidationUtils.normalizeEmail(request.getParameter("email"));
        
        // Llamada al servicio
        String result = authService.resetUserAttempts(cleanEmail); 

        JSONObject jsonRes = new JSONObject();
        response.setContentType("application/json");

        if ("SUCCESS".equals(result)) {
            jsonRes.put("status", "success");
            jsonRes.put("message", "Intentos reiniciados correctamente.");
        } else {
            jsonRes.put("status", "error");
            jsonRes.put("message", result);
        }
        
        response.getWriter().write(jsonRes.toString());
    }

    // ==========================================
    // 4. GESTIÓN DE CONTRASEÑAS
    // ==========================================

    private void handleRequestPasswordChange(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String uuid = request.getParameter("uuid");
        String currentPass = request.getParameter("currentPassword");
        
        String result = authService.requestPasswordChange(uuid, currentPass);
        
        JSONObject jsonRes = new JSONObject();
        response.setContentType("application/json");

        if ("SUCCESS".equals(result)) {
            jsonRes.put("status", "success");
            jsonRes.put("message", "Código de verificación enviado a tu correo.");
        } else {
            jsonRes.put("status", "error");
            jsonRes.put("message", result);
        }
        response.getWriter().write(jsonRes.toString());
    }

    private void handleConfirmPasswordChange(HttpServletRequest request, HttpServletResponse response, String ip, String userAgent) 
            throws IOException {
        
        String result = authService.confirmPasswordChange(
            request.getParameter("uuid"),
            request.getParameter("code"),
            request.getParameter("newPassword"),
            ip, userAgent
        );

        JSONObject jsonRes = new JSONObject();
        response.setContentType("application/json");

        if ("SUCCESS".equals(result)) {
            jsonRes.put("status", "success");
            jsonRes.put("message", "Contraseña actualizada correctamente.");
        } else {
            jsonRes.put("status", "error");
            jsonRes.put("message", result);
        }
        response.getWriter().write(jsonRes.toString());
    }
}