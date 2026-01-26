package app.utils;

import java.security.SecureRandom;
import java.util.UUID;

public class TokenUtils {

    // Genera un código de 6 dígitos numéricos (ej. 542189)
    public static String generateNumericToken() {
        SecureRandom random = new SecureRandom();
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }

    // Genera un UUID para el campo UuidUser
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
}