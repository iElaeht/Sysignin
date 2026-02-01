package app.controllers;

import app.dao.NotificationDAO;
import app.models.Notification;
import app.models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/notifications/*")
public class NotificationServlet extends HttpServlet {

    private NotificationDAO notificationDAO = new NotificationDAO();

    // ======================================================
    // 1. MÉTODOS DE CICLO DE VIDA (GET / POST)
    // ======================================================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        User currentUser = (User) request.getSession().getAttribute("user");

        // Seguridad: Verificar sesión activa
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        switch (pathInfo != null ? pathInfo : "") {
            case "/list":
                handleListNotifications(request, response, currentUser);
                break;
            case "/unread-count":
                handleUnreadCount(request, response, currentUser);
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

        // Seguridad: Verificar sesión activa
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if ("/mark-as-read".equals(pathInfo)) {
            handleMarkAsRead(request, response, currentUser);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // ======================================================
    // 2. CONSULTAS Y VISUALIZACIÓN
    // ======================================================

    /**
     * Carga la lista de notificaciones para el panel del usuario.
     */
    private void handleListNotifications(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException {
        
        // Recuperamos las notificaciones vinculadas al UUID del usuario
        List<Notification> list = notificationDAO.getByUser(user.getUuidUser());
        
        request.setAttribute("notifications", list);
        request.getRequestDispatcher("/notifications-panel.jsp").forward(request, response);
    }

    /**
     * Retorna el conteo de notificaciones no leídas (para el indicador UI).
     */
    private void handleUnreadCount(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        int count = notificationDAO.countUnread(user.getUuidUser());
        
        // Respuesta plana optimizada para llamadas AJAX/Fetch
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.valueOf(count));
    }

    // ======================================================
    // 3. ACCIONES Y ESTADOS
    // ======================================================

    /**
     * Cambia el estado de una notificación específica a 'leída'.
     */
    private void handleMarkAsRead(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        String idParam = request.getParameter("id");

        if (idParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("ERROR: ID requerido");
            return;
        }

        try {
            int idNotification = Integer.parseInt(idParam);
            
            // Sincronización con el DAO: Se valida que la notificación pertenezca al usuario en sesión
            if (notificationDAO.markAsRead(idNotification, user.getUuidUser())) {
                response.getWriter().write("SUCCESS");
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("ERROR: No autorizada o no encontrada");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("INVALID_ID");
        }
    }
}