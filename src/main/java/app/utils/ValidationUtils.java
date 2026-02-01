package app.utils;

import java.util.regex.Pattern;

/**
 * UTIL: ValidationUtils
 * Descripción: Filtros de limpieza y validación de formatos para prevenir datos corruptos o ataques.
 */
public class ValidationUtils {

    // Regex más robusto para emails (RFC 5322)
    private static final String EMAIL_PATTERN = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

    /**
     * Limpia el email: quita espacios internos, externos y lo pasa a minúsculas.
     */
    public static String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase().replaceAll("\\s+", "");
    }

    /**
     * Normaliza el token: elimina espacios y fuerza mayúsculas.
     * Útil porque los usuarios suelen escribir tokens en minúsculas por error.
     */
    public static String normalizeToken(String token) {
        if (token == null) return null;
        return token.trim().toUpperCase().replaceAll("\\s+", "");
    }

    /**
     * Valida si el formato de email es correcto.
     */
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return Pattern.compile(EMAIL_PATTERN).matcher(email).matches();
    }

    /**
     * Verifica que el texto no contenga etiquetas HTML o scripts maliciosos.
     * Protección básica contra XSS.
     */
    public static boolean isSafeText(String text) {
        if (text == null) return false;
        
        // Detecta cualquier par de etiquetas <...>
        boolean containsTags = text.matches(".*<[^>]*>.*");
        
        // Detecta inyecciones comunes de scripts
        String lower = text.toLowerCase();
        boolean hasJavascript = lower.contains("javascript:") 
                             || lower.contains("onclick=") 
                             || lower.contains("onerror=")
                             || lower.contains("<script")
                             || lower.contains("eval(");
                             
        return !containsTags && !hasJavascript;
    }
}