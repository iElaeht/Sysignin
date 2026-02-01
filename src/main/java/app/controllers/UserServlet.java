package app.controllers;

import app.dao.UserDAO;
import app.dao.AuditDAO;
import app.models.User;
import app.utils.NetUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/user/*")
public class UserServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();
    private AuditDAO auditDAO = new AuditDAO();

    // ======================================================
    // 1. CONTROL DE ACCIONES (POST)
    // ======================================================

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        String ip = NetUtils.getClientIp(request);
        String userAgent = NetUtils.getUserAgent(request);

        // Recuperamos el usuario de sesión (Previamente validado por AuthFilter)
        HttpSession session = request.getSession(false);
        User currentUser = (User) session.getAttribute("user");

        switch (pathInfo != null ? pathInfo : "") {
            case "/update-profile":
                handleUpdateProfile(request, response, currentUser, ip, userAgent);
                break;
            case "/delete-account":
                handleSoftDelete(request, response, currentUser, ip, userAgent);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    // ======================================================
    // 2. GESTIÓN DEL PERFIL
    // ======================================================

    /**
     * Procesa la actualización de datos personales del usuario.
     */
    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response, User user, String ip, String userAgent) 
            throws IOException {
        
        // Extracción de parámetros
        String newUsername = request.getParameter("username");
        String newPhone = request.getParameter("phone");
        String newGender = request.getParameter("gender");
        String newCountry = request.getParameter("country");
        String newCity = request.getParameter("city");

        // VALIDACIÓN DE INTEGRIDAD
        if (isInvalidUsername(newUsername)) {
            response.getWriter().write("ERROR: Nombre de usuario inválido (Máx 40 caracteres).");
            return;
        }

        // Sincronización del Objeto Model
        user.setUsername(newUsername);
        user.setPhoneNumber(newPhone);
        user.setGender(newGender);
        user.setCountry(newCountry != null ? newCountry : user.getCountry());
        user.setCity(newCity != null ? newCity : user.getCity());

        // Persistencia
        if (userDAO.updateProfile(user)) {
            // Actualización de la sesión para reflejar cambios en la UI inmediatamente
            request.getSession().setAttribute("user", user);
            
            auditDAO.insertLog(user.getIdUser(), user.getUuidUser(), "PROFILE_UPDATE", ip, userAgent, "Perfil actualizado exitosamente.");
            response.getWriter().write("SUCCESS");
        } else {
            response.getWriter().write("ERROR: Error técnico al guardar en base de datos.");
        }
    }

    // ======================================================
    // 3. SEGURIDAD Y ESTADOS DE CUENTA
    // ======================================================

    /**
     * Realiza un borrado lógico de la cuenta, invalidando el acceso pero manteniendo la integridad referencial.
     */
    private void handleSoftDelete(HttpServletRequest request, HttpServletResponse response, User user, String ip, String userAgent) 
            throws IOException {
        
        if (userDAO.softDelete(user.getUuidUser())) {
            auditDAO.insertLog(user.getIdUser(), user.getUuidUser(), "ACCOUNT_DELETION", ip, userAgent, "Solicitud de baja de cuenta procesada.");
            
            // Seguridad: Destrucción de sesión tras desactivación
            request.getSession().invalidate();
            response.getWriter().write("SUCCESS: Tu cuenta ha sido desactivada.");
        } else {
            response.getWriter().write("ERROR: No se pudo procesar la desactivación.");
        }
    }

    // ======================================================
    // 4. UTILIDADES PRIVADAS
    // ======================================================

    private boolean isInvalidUsername(String username) {
        return username == null || username.trim().isEmpty() || username.length() > 40;
    }
}