package app.controllers;

import app.dao.UserDAO;
import app.dao.AuditDAO;
import app.models.User;
import app.models.AuditLog;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/*")
public class AdminServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();
    private AuditDAO auditDAO = new AuditDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        User currentUser = (User) request.getSession().getAttribute("user");

        // VALIDACIÓN DE ROL: El AuthFilter deja entrar, pero aquí restringimos por Rol
        if (currentUser == null || !"Admin".equalsIgnoreCase(currentUser.getRoles())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado: Se requieren permisos de administrador.");
            return;
        }

        String action = (pathInfo != null) ? pathInfo : "";

        switch (action) {
            case "/dashboard":
                handleAdminDashboard(request, response);
                break;
            case "/user-list":
                handleListAllUsers(request, response);
                break;
            case "/audit-global":
                handleGlobalAudit(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        User currentUser = (User) request.getSession().getAttribute("user");

        // Verificación de seguridad también en POST
        if (currentUser == null || !"Admin".equalsIgnoreCase(currentUser.getRoles())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if ("/update-status".equals(pathInfo)) {
            handleUpdateUserStatus(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleAdminDashboard(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Aquí podrías cargar contadores rápidos si fuera necesario
        request.getRequestDispatcher("/admin-dashboard.jsp").forward(request, response);
    }

    private void handleListAllUsers(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Obtenemos la lista de todos los usuarios registrados
        List<User> users = userDAO.getAllUsers(); 
        request.setAttribute("userList", users);
        request.getRequestDispatcher("/admin-users.jsp").forward(request, response);
    }

    private void handleGlobalAudit(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Llamada al método corregido en AuditDAO
        List<AuditLog> logs = auditDAO.getGlobalLogs(); 
        request.setAttribute("globalLogs", logs);
        request.getRequestDispatcher("/admin-audit.jsp").forward(request, response);
    }

    private void handleUpdateUserStatus(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String uuid = request.getParameter("uuid");
        String newStatus = request.getParameter("status"); // Ejemplo: 'Banned', 'Active'

        if (uuid != null && newStatus != null) {
            if (userDAO.updateStatus(uuid, newStatus)) {
                response.getWriter().write("SUCCESS: Estado actualizado.");
            } else {
                response.getWriter().write("ERROR: No se pudo actualizar el estado.");
            }
        } else {
            response.getWriter().write("ERROR: Datos incompletos.");
        }
    }
}