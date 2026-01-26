package app.controllers;

import app.dao.SessionDAO;
import app.dao.SecurityTokenDAO;
import app.models.User;
import app.models.UserSession;
import app.models.SecurityToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/security/*")
public class SecurityServlet extends HttpServlet {

    private SessionDAO sessionDAO = new SessionDAO();
    private SecurityTokenDAO tokenDAO = new SecurityTokenDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        User currentUser = (User) request.getSession().getAttribute("user");

        // El AuthFilter ya validó que currentUser no sea null
        switch (pathInfo != null ? pathInfo : "") {
            case "/sessions":
                handleListSessions(request, response, currentUser);
                break;
            case "/token-history":
                handleTokenHistory(request, response, currentUser);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        User currentUser = (User) request.getSession().getAttribute("user");

        if ("/terminate-session".equals(pathInfo)) {
            handleTerminateSession(request, response, currentUser);
        }
    }

    private void handleListSessions(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException {
        
        // Obtenemos todas las sesiones activas del usuario
        List<UserSession> sessions = sessionDAO.getActiveSessions(user.getUuidUser());
        
        // Pasamos la lista al JSP de seguridad
        request.setAttribute("activeSessions", sessions);
        request.getRequestDispatcher("/security-center.jsp").forward(request, response);
    }

    private void handleTerminateSession(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        int idSession = Integer.parseInt(request.getParameter("idSession"));
        
        // Intentamos terminar la sesión (el DAO valida que pertenezca al usuario)
        if (sessionDAO.terminateSession(idSession, user.getUuidUser())) {
            response.getWriter().write("SUCCESS: Sesión finalizada.");
        } else {
            response.getWriter().write("ERROR: No se pudo cerrar la sesión.");
        }
    }

    private void handleTokenHistory(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException {
        
        // Historial de cambios críticos (emails, contraseñas solicitadas)
        List<SecurityToken> tokens = tokenDAO.getTokensByUser(user.getUuidUser());
        request.setAttribute("tokenHistory", tokens);
        request.getRequestDispatcher("/token-log.jsp").forward(request, response);
    }
}