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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        String ip = NetUtils.getClientIp(request);
        String userAgent = NetUtils.getUserAgent(request);

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

    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response, User user, String ip, String userAgent) 
            throws IOException {
        
        String newUsername = request.getParameter("username");
        String newPhone = request.getParameter("phone");
        String newGender = request.getParameter("gender");
        String newCountry = request.getParameter("country");
        String newCity = request.getParameter("city");

        if (isInvalidUsername(newUsername)) {
            response.getWriter().write("ERROR: Nombre de usuario inválido.");
            return;
        }

        user.setUsername(newUsername);
        user.setPhoneNumber(newPhone);
        user.setGender(newGender);
        user.setCountry(newCountry != null ? newCountry : user.getCountry());
        user.setCity(newCity != null ? newCity : user.getCity());

        // Preparamos la ubicación para el log
        String location = user.getCity() + ", " + user.getCountry();

        if (userDAO.updateProfile(user)) {
            request.getSession().setAttribute("user", user);
            
            // LLAMADA ACTUALIZADA: Ahora con Email y Location
            auditDAO.insertLog(
                user.getIdUser(), 
                user.getUuidUser(), 
                user.getEmail(), 
                "PROFILE_UPDATE", 
                ip, 
                location, 
                userAgent, 
                "Perfil actualizado exitosamente."
            );
            
            response.getWriter().write("SUCCESS");
        } else {
            response.getWriter().write("ERROR: Error técnico al guardar.");
        }
    }

    // ======================================================
    // 3. SEGURIDAD Y ESTADOS DE CUENTA
    // ======================================================

    private void handleSoftDelete(HttpServletRequest request, HttpServletResponse response, User user, String ip, String userAgent) 
            throws IOException {
        
        String location = user.getCity() + ", " + user.getCountry();

        if (userDAO.softDelete(user.getUuidUser())) {
            // LLAMADA ACTUALIZADA: Ahora con Email y Location
            auditDAO.insertLog(
                user.getIdUser(), 
                user.getUuidUser(), 
                user.getEmail(), 
                "ACCOUNT_DELETION", 
                ip, 
                location, 
                userAgent, 
                "Solicitud de baja de cuenta procesada."
            );
            
            request.getSession().invalidate();
            response.getWriter().write("SUCCESS: Tu cuenta ha sido desactivada.");
        } else {
            response.getWriter().write("ERROR: No se pudo procesar la desactivación.");
        }
    }

    private boolean isInvalidUsername(String username) {
        return username == null || username.trim().isEmpty() || username.length() > 40;
    }
}