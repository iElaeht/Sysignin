package app.filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

// Este filtro intercepta todas las peticiones (/*)
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        String loginURI = httpRequest.getContextPath() + "/login.jsp";
        String registerURI = httpRequest.getContextPath() + "/register.jsp";
        String authServletURI = httpRequest.getContextPath() + "/auth"; // Permitir llamadas al servlet de auth

        // 1. Determinar si la petición es para una página pública o recursos estáticos
        boolean isLoginRequest = httpRequest.getRequestURI().equals(loginURI);
        boolean isRegisterRequest = httpRequest.getRequestURI().equals(registerURI);
        boolean isAuthServlet = httpRequest.getRequestURI().startsWith(authServletURI);
        boolean isStaticResource = httpRequest.getRequestURI().contains("/assets/") || 
                                    httpRequest.getRequestURI().endsWith(".css") || 
                                    httpRequest.getRequestURI().endsWith(".js");

        // 2. Verificar si el usuario está logueado (objeto "user" en sesión)
        boolean isLoggedIn = (session != null && session.getAttribute("user") != null);

        // 3. Lógica de redirección
        if (isLoggedIn || isLoginRequest || isRegisterRequest || isAuthServlet || isStaticResource) {
            // Si está logueado o va a una página permitida, dejarlo pasar
            chain.doFilter(request, response);
        } else {
            // Si no está logueado y quiere entrar a una ruta privada, mandarlo al login
            httpResponse.sendRedirect(loginURI + "?error=unauthorized");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}