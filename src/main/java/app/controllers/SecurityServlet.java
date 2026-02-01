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

        switch (pathInfo != null ? pathInfo : "") {
            case "/terminate-session":
                handleTerminateSession(request, response, currentUser);
                break;
            case "/terminate-others": // NUEVA: Para cerrar todo lo demás
                handleTerminateOtherSessions(request, response, currentUser);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }
    private void handleListSessions(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException {
        
        // 1. Pedimos al DAO las sesiones que están IsActive = true
        List<UserSession> sessions = sessionDAO.getActiveSessions(user.getUuidUser());
        
        // 2. Pasamos la lista al request para que el JSP pueda iterarla
        request.setAttribute("activeSessions", sessions);
        
        // 3. Enviamos al usuario a la vista de seguridad
        request.getRequestDispatcher("/security-center.jsp").forward(request, response);
    }

    private void handleTerminateSession(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        String idParam = request.getParameter("idSession");
        if (idParam == null) {
            response.getWriter().write("ERROR: ID de sesión ausente.");
            return;
        }

        int idSession = Integer.parseInt(idParam);
        // Obtenemos el ID de la sesión actual guardado en el login
        Integer currentSessionId = (Integer) request.getSession().getAttribute("idSession");

        if (currentSessionId != null && currentSessionId == idSession) {
            response.getWriter().write("ERROR: No puedes cerrar tu sesión actual desde aquí. Usa Logout.");
            return;
        }

        if (sessionDAO.terminateSession(idSession, user.getUuidUser())) {
            response.getWriter().write("SUCCESS");
        } else {
            response.getWriter().write("ERROR: No se pudo cerrar la sesión.");
        }
    }
    private void handleTerminateOtherSessions(HttpServletRequest request, HttpServletResponse response, User user) 
        throws IOException {
    
    Integer currentSessionId = (Integer) request.getSession().getAttribute("idSession");
    
    if (currentSessionId != null && sessionDAO.terminateOtherSessions(user.getUuidUser(), currentSessionId)) {
        response.getWriter().write("SUCCESS: Todas las demás sesiones han sido cerradas.");
    } else {
        response.getWriter().write("ERROR: No se pudo completar la acción.");
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