package app.utils;

// Cambiamos javax por jakarta
import jakarta.servlet.http.HttpServletRequest;

public class NetUtils {

    public static String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }
    public static String getUserAgent(HttpServletRequest request) {
        return (request != null) ? request.getHeader("User-Agent") : "Unknown";
    }
}