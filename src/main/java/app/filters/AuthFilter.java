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
    public void doFilter(ServletRequest request, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = req.getRequestURI().substring(req.getContextPath().length());

        // ==========================================
        // 1. CABECERAS DE SEGURIDAD (Hardening Inmediato)
        // ==========================================
        // IMPORTANTE: Esto debe ir antes de cualquier lógica para evitar el "caché" del botón atrás.
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); 
        response.setHeader("Pragma", "no-cache"); 
        response.setDateHeader("Expires", 0); 
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // 2. OBTENER SESIÓN Y USUARIO
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // 3. DEFINICIÓN DE RUTAS PÚBLICAS
        boolean isPublicPath = path.startsWith("/auth/") || 
                               path.startsWith("/assets/") || 
                               path.contains("/public/") || 
                               path.equals("/index.jsp") || 
                               path.equals("/") || 
                               path.isEmpty();

        // 4. LÓGICA DE REDIRECCIÓN INVERSA (Evitar ver login/registro si ya estoy logueado)
        if (isPublicPath) {
            if (user != null && (path.equals("/") || path.contains("login") || path.contains("register"))) {
                // NOTA: Cambia "/dashboard/home" por la ruta real que NO te dé 404
                response.sendRedirect(req.getContextPath() + "/dashboard");
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        // 5. VERIFICACIÓN DE SESIÓN PARA RUTAS PROTEGIDAS
        if (user == null) {
            String requestedWith = req.getHeader("X-Requested-With");
            
            if ("XMLHttpRequest".equals(requestedWith)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"status\":\"error\", \"message\":\"Sesión expirada\"}");
            } else {
                String targetUrl = req.getRequestURI();
                if (req.getQueryString() != null) targetUrl += "?" + req.getQueryString();
                
                HttpSession targetSession = req.getSession(true); 
                targetSession.setAttribute("target_url", targetUrl);
                
                response.sendRedirect(req.getContextPath() + "/auth/loginView");
            }
            return;
        }

        // 6. SEGURIDAD POR ROLES
        if (path.startsWith("/admin/") && !"Admin".equalsIgnoreCase(user.getRoles())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Permisos insuficientes");
            return;
        }

        // Si todo está bien, adelante
        chain.doFilter(request, response);
    }

    @Override public void init(FilterConfig config) {}
    @Override public void destroy() {}
}