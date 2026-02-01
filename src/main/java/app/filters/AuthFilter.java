package app.filters;

import app.models.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getRequestURI().substring(req.getContextPath().length());

        // 1. RECURSOS PÚBLICOS (Sin restricciones)
        boolean isPublicPath = path.startsWith("/auth/") || 
                               path.startsWith("/assets/") || 
                               path.contains("/public/") || 
                               path.equals("/index.jsp") ||
                               path.equals("/") ||
                               path.isEmpty();

        if (isPublicPath) {
            chain.doFilter(request, response);
            return;
        }

        // 2. VERIFICAR SESIÓN
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            // Si es una petición AJAX, enviamos 401, si es navegación, redirigimos
            String requestedWith = req.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(requestedWith)) {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sesión expirada");
            } else {
                res.sendRedirect(req.getContextPath() + "/auth/loginView");
            }
            return;
        }

        // 3. SEGURIDAD POR ROLES (El toque maestro)
        // Bloqueamos acceso a /admin/* si el usuario no es Admin
        if (path.startsWith("/admin/") && !"Admin".equalsIgnoreCase(user.getRoles())) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "No tienes permisos de administrador.");
            return;
        }

        // Si pasó todas las pruebas, adelante
        chain.doFilter(request, response);
    }
}