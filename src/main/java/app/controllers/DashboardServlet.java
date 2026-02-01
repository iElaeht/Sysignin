package app.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Verificamos si hay sesión activa (esto también lo hace el Filter, pero por seguridad)
        HttpSession session = request.getSession(false);
        
        if (session != null && session.getAttribute("user") != null) {
            // Despachamos la vista que está protegida en WEB-INF
            request.getRequestDispatcher("/WEB-INF/views/dashboard/main.jsp").forward(request, response);
        } else {
            // Si no hay sesión, al login
            response.sendRedirect(request.getContextPath() + "/auth/loginView");
        }
    }
}