package app.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * UTIL: NetUtils
 * Descripción: Utilidades de red para capturar IP, User-Agent e información del cliente.
 */
public class NetUtils {

    /**
     * Obtiene la IP real del cliente, incluso detrás de proxies o balanceadores de carga.
     */
    public static String getClientIp(HttpServletRequest request) {
        String[] headers = {
            "CF-Connecting-IP", // Prioridad para Cloudflare
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_CLIENT_IP",
            "X-Real-IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (isValidIp(ip)) {
                // Si hay una cadena de IPs (proxies), tomamos la primera (la original del cliente)
                return ip.split(",")[0].trim();
            }
        }

        String remoteIp = request.getRemoteAddr();
        
        // Normalización de Localhost de IPv6 a IPv4
        if ("0:0:0:0:0:0:0:1".equals(remoteIp) || "::1".equals(remoteIp)) {
            return "127.0.0.1"; 
        }
        
        return (remoteIp != null) ? remoteIp : "0.0.0.0";
    }

    /**
     * Obtiene el User-Agent completo del navegador.
     */
    public static String getUserAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        return (ua != null && !ua.isEmpty()) ? ua : "Desconocido";
    }

    /**
     * Método extra: Clasifica el tipo de dispositivo de forma rápida.
     * Útil para los logs de auditoría y alertas de inicio de sesión.
     */
    public static String getDeviceType(HttpServletRequest request) {
        String ua = getUserAgent(request).toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "Móvil";
        } else if (ua.contains("tablet") || ua.contains("ipad")) {
            return "Tablet";
        }
        return "Escritorio";
    }

    /**
     * Valida si una IP obtenida de los headers es válida.
     */
    private static boolean isValidIp(String ip) {
        return ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip);
    }
}