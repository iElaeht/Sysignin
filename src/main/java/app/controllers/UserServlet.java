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

        // Obtenemos el usuario de la sesión (el AuthFilter garantiza que no sea null)
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

    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response, User user, String ip, String userAgent) 
            throws IOException {
        
        // 1. Capturamos los nuevos datos del formulario
        String newUsername = request.getParameter("username");
        String newPhone = request.getParameter("phone");
        String newGender = request.getParameter("gender");

        // 2. Actualizamos el objeto temporalmente
        user.setUsername(newUsername);
        user.setPhoneNumber(newPhone);
        user.setGender(newGender);

        // 3. Persistimos en la Base de Datos
        if (userDAO.updateProfile(user)) {
            // Actualizamos el usuario en la sesión para que los cambios se vean reflejados en el JSP
            request.getSession().setAttribute("user", user);
            
            auditDAO.insertLog(user.getIdUser(), user.getUuidUser(), "PROFILE_UPDATE", ip, userAgent, "Datos básicos actualizados.");
            response.getWriter().write("SUCCESS: Perfil actualizado correctamente.");
        } else {
            response.getWriter().write("ERROR: No se pudo actualizar el perfil.");
        }
    }

    private void handleSoftDelete(HttpServletRequest request, HttpServletResponse response, User user, String ip, String userAgent) 
            throws IOException {
        
        // El borrado lógico cambia el estado a 'Banned' o 'Inactive' y marca isDeleted = true
        if (userDAO.softDelete(user.getUuidUser())) {
            auditDAO.insertLog(user.getIdUser(), user.getUuidUser(), "ACCOUNT_DELETION", ip, userAgent, "El usuario solicitó borrar su cuenta.");
            
            // Al borrar la cuenta, invalidamos la sesión
            request.getSession().invalidate();
            response.getWriter().write("SUCCESS: Tu cuenta ha sido desactivada.");
        } else {
            response.getWriter().write("ERROR: No se pudo procesar la solicitud.");
        }
    }
}