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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        User currentUser = (User) request.getSession().getAttribute("user");

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
        
        if ("/mark-as-read".equals(pathInfo)) {
            handleMarkAsRead(request, response);
        }
    }

    private void handleListNotifications(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException {
        
        // Obtenemos las últimas 20 notificaciones
        List<Notification> list = notificationDAO.getByUser(user.getUuidUser());
        
        // Las pasamos al JSP o podrías convertirlas a JSON aquí mismo
        request.setAttribute("notifications", list);
        request.getRequestDispatcher("/notifications-panel.jsp").forward(request, response);
    }

    private void handleUnreadCount(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        int count = notificationDAO.countUnread(user.getUuidUser());
        
        // Retornamos solo el número (ideal para el globito rojo de la UI)
        response.setContentType("text/plain");
        response.getWriter().write(String.valueOf(count));
    }

    private void handleMarkAsRead(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        try {
            int idNotification = Integer.parseInt(request.getParameter("id"));
            if (notificationDAO.markAsRead(idNotification)) {
                response.getWriter().write("SUCCESS");
            } else {
                response.getWriter().write("ERROR");
            }
        } catch (NumberFormatException e) {
            response.setStatus(400);
            response.getWriter().write("INVALID_ID");
        }
    }
}