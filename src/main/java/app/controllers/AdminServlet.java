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

    // ======================================================
    // 1. MÉTODOS DE CICLO DE VIDA Y CONTROL DE ACCESO
    // ======================================================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Control de Seguridad: Verificación de Rol Administrativo
        if (!isAdmin(request, response)) return;

        String pathInfo = request.getPathInfo();
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
        
        // Control de Seguridad: Verificación de Rol Administrativo
        if (!isAdmin(request, response)) return;

        String pathInfo = request.getPathInfo();

        switch (pathInfo != null ? pathInfo : "") {
            case "/update-status":
                handleUpdateUserStatus(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    /**
     * Valida si el usuario en sesión tiene privilegios de Administrador.
     */
    private boolean isAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null || !"Admin".equalsIgnoreCase(currentUser.getRoles())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado: Se requieren permisos de administrador.");
            return false;
        }
        return true;
    }

    // ======================================================
    // 2. GESTIÓN DE VISTAS (DASHBOARD Y NAVEGACIÓN)
    // ======================================================

    private void handleAdminDashboard(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Espacio para cargar estadísticas rápidas (ej. total usuarios, logs hoy)
        request.getRequestDispatcher("/admin-dashboard.jsp").forward(request, response);
    }

    // ======================================================
    // 3. GESTIÓN DE USUARIOS
    // ======================================================

    private void handleListAllUsers(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Recuperar la lista completa desde la persistencia
        List<User> users = userDAO.getAllUsers(); 
        request.setAttribute("userList", users);
        request.getRequestDispatcher("/admin-users.jsp").forward(request, response);
    }

    private void handleUpdateUserStatus(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String uuid = request.getParameter("uuid");
        String newStatus = request.getParameter("status");

        if (uuid != null && newStatus != null) {
            // Sincronizado con UserDAO.changeState
            if (userDAO.changeState(uuid, newStatus)) {
                response.getWriter().write("SUCCESS: Estado actualizado.");
            } else {
                response.getWriter().write("ERROR: No se pudo actualizar el estado.");
            }
        }
    }

    // ======================================================
    // 4. MONITOREO Y AUDITORÍA
    // ======================================================

    private void handleGlobalAudit(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Obtener historial de acciones de todo el sistema
        List<AuditLog> logs = auditDAO.getGlobalLogs(); 
        request.setAttribute("globalLogs", logs);
        request.getRequestDispatcher("/admin-audit.jsp").forward(request, response);
    }
}