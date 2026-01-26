package app.controllers;

import app.models.User;
import app.services.AuthService;
import app.utils.NetUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/auth/*")
public class AuthServlet extends HttpServlet {

    private AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        String ip = NetUtils.getClientIp(request);
        String userAgent = NetUtils.getUserAgent(request);

        // Limpieza del path para evitar errores de null
        String action = (pathInfo != null) ? pathInfo : "";

        switch (action) {
            case "/register":
                handleRegister(request, response, ip, userAgent);
                break;
            case "/login":
                handleLogin(request, response, ip, userAgent);
                break;
            case "/verify":
                handleVerify(request, response, ip, userAgent);
                break;
            case "/request-email-change":
                handleRequestEmailChange(request, response, ip, userAgent);
                break;
            case "/confirm-email-change":
                handleConfirmEmailChange(request, response, ip, userAgent);
                break;
            case "/request-password-change":
                handleRequestPasswordChange(request, response, ip, userAgent);
                break;
            case "/confirm-password-change":
                handleConfirmPasswordChange(request, response, ip, userAgent);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    // --- MÉTODOS DE ACCESO ---

    private void handleRegister(HttpServletRequest request, HttpServletResponse response, String ip, String userAgent) 
            throws IOException {
        String result = authService.registerUser(
            request.getParameter("username"),
            request.getParameter("email"),
            request.getParameter("password"),
            ip, "Peru", "Lima", userAgent
        );
        response.getWriter().write(result);
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response, String ip, String userAgent) 
            throws IOException {
        User user = authService.login(
            request.getParameter("email"),
            request.getParameter("password"),
            ip, userAgent, "Peru", "Lima", "Web"
        );

        if (user != null) {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            response.getWriter().write("SUCCESS: Bienvenido " + user.getUsername());
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("ERROR: Credenciales inválidas.");
        }
    }

    private void handleVerify(HttpServletRequest request, HttpServletResponse response, String ip, String userAgent) 
            throws IOException {
        String result = authService.verifyAccount(
            request.getParameter("email"),
            request.getParameter("token"),
            ip, userAgent
        );
        response.getWriter().write(result);
    }

    // --- MÉTODOS DE SEGURIDAD (CAMBIO DE DATOS) ---

    private void handleRequestEmailChange(HttpServletRequest request, HttpServletResponse response, String ip, String userAgent) 
            throws IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) { response.sendError(401); return; }

        String result = authService.requestEmailChange(
            user.getUuidUser(),
            request.getParameter("current_password"),
            request.getParameter("new_email"),
            ip, userAgent
        );
        response.getWriter().write(result);
    }

    private void handleConfirmEmailChange(HttpServletRequest request, HttpServletResponse response, String ip, String userAgent) 
            throws IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) { response.sendError(401); return; }

        String result = authService.confirmEmailChange(
            user.getUuidUser(),
            request.getParameter("token"),
            ip, userAgent
        );
        response.getWriter().write(result);
    }

    private void handleRequestPasswordChange(HttpServletRequest request, HttpServletResponse response, String ip, String userAgent) 
            throws IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) { response.sendError(401); return; }

        String result = authService.requestPasswordChange(
            user.getUuidUser(),
            request.getParameter("current_password"),
            ip, userAgent
        );
        response.getWriter().write(result);
    }

    private void handleConfirmPasswordChange(HttpServletRequest request, HttpServletResponse response, String ip, String userAgent) 
            throws IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) { response.sendError(401); return; }

        String result = authService.confirmPasswordChange(
            user.getUuidUser(),
            request.getParameter("token"),
            request.getParameter("new_password"),
            ip, userAgent
        );
        response.getWriter().write(result);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        if ("/logout".equals(request.getPathInfo())) {
            HttpSession session = request.getSession(false);
            if (session != null) session.invalidate();
            response.getWriter().write("SUCCESS: Sesión cerrada.");
        }
    }
}