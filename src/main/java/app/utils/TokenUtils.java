package app.utils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TokenUtils {

    // Genera el token de 10 dígitos para el correo
    public static String generateNumericToken() {
        long number = ThreadLocalRandom.current().nextLong(1000000000L, 10000000000L);
        return String.valueOf(number);
    }

    // Genera el UUID único para el UuidUser
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
}