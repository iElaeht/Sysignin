package app.utils;

public class ValidationUtils {

    public static boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email != null && email.matches(regex);
    }

    public static boolean isSafeText(String text) {
        // Evita scripts básicos o etiquetas HTML
        if (text == null) return false;
        String lower = text.toLowerCase();
        return !lower.contains("<script>") && !lower.contains("href=");
    }
}