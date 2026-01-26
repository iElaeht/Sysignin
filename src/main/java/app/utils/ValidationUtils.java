package app.utils;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String PASS_REGEX = "^(?=.*[0-9])(?=.*[A-Z]).{8,}$";

    public static String normalizeEmail(String email) {
        if (email == null) return "";
        return email.trim().toLowerCase();
    }
    public static String normalizePassword(String password) {
        if (password == null) return "";
        return password.trim();
    }

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return Pattern.compile(EMAIL_REGEX).matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null) return false;
        return Pattern.compile(PASS_REGEX).matcher(password).matches();
    }
}