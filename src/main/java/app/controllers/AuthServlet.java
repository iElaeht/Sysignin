package app.controllers;

import app.models.User;
import app.services.AuthService;
import app.utils.NetUtils;
import app.utils.ValidationUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

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
        // Normalizamos el identificador por si es un correo con espacios accidentales
        String identifier = ValidationUtils.normalizeEmail(request.getParameter("identifier")); 
        String password = request.getParameter("password");

        User user = authService.login(identifier, password, ip, userAgent, "Web", "Lima");

        if (user != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setAttribute("idSession", user.getIdUser()); 
            response.getWriter().write("SUCCESS");
        } else {
            response.setStatus(401);
            response.getWriter().write("ERROR: Credenciales inválidas o cuenta penalizada.");
        }
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
        
        // 1. Recoger y Normalizar
        String rawUser = request.getParameter("username");
        String rawEmail = request.getParameter("email");
        String password = request.getParameter("password");

        String cleanEmail = ValidationUtils.normalizeEmail(rawEmail);
        String cleanUser = (rawUser != null) ? rawUser.trim().replaceAll("\\s+", "") : "";

        // 2. Validar Seguridad (Protección contra Inyección/Scripts)
        if (!ValidationUtils.isSafeText(cleanUser)) {
            processResult(response, "ERROR: El nombre de usuario contiene caracteres prohibidos.");
            return;
        }

        if (!ValidationUtils.isValidEmail(cleanEmail)) {
            processResult(response, "ERROR: El formato del correo es inválido.");
            return;
        }

        // 3. Enviar al Servicio con datos limpios
        String result = authService.registerUser(cleanUser, cleanEmail, password, ip, "Peru", "Lima");
        processResult(response, result);
    }

    private void handleVerify(HttpServletRequest request, HttpServletResponse response, String ip, String userAgent) 
            throws IOException {
        
        String cleanEmail = ValidationUtils.normalizeEmail(request.getParameter("email"));
        String cleanToken = ValidationUtils.normalizeToken(request.getParameter("token"));

        String result = authService.verifyAccount(cleanEmail, cleanToken, ip, userAgent);
        processResult(response, result);
    }

    private void handleResendToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String cleanEmail = ValidationUtils.normalizeEmail(request.getParameter("email"));
        
        if (cleanEmail == null || cleanEmail.isEmpty()) {
            processResult(response, "ERROR: Email requerido.");
            return;
        }
        String result = authService.resendToken(cleanEmail);
        processResult(response, result);
    }

    private void handleResetAttempts(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String cleanEmail = ValidationUtils.normalizeEmail(request.getParameter("email"));
        String result = authService.resetUserAttempts(cleanEmail); 
        processResult(response, result);
    }

    // ==========================================
    // 4. GESTIÓN DE CONTRASEÑAS
    // ==========================================

    private void handleRequestPasswordChange(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String uuid = request.getParameter("uuid");
        String currentPass = request.getParameter("currentPassword");
        String result = authService.requestPasswordChange(uuid, currentPass);
        processResult(response, result);
    }

    private void handleConfirmPasswordChange(HttpServletRequest request, HttpServletResponse response, String ip, String userAgent) 
            throws IOException {
        String result = authService.confirmPasswordChange(
            request.getParameter("uuid"),
            request.getParameter("code"),
            request.getParameter("newPassword"),
            ip, userAgent
        );
        processResult(response, result);
    }

    // ==========================================
    // 5. UTILIDADES DE RESPUESTA
    // ==========================================

    private void processResult(HttpServletResponse response, String result) throws IOException {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        if (result != null && result.contains("SUCCESS")) {
            response.setStatus(200);
        } else {
            response.setStatus(400);
        }
        response.getWriter().write(result);
    }
}