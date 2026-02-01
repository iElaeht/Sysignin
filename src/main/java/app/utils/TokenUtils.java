package app.utils;

import java.util.UUID;
import java.security.SecureRandom;

/**
 * UTIL: TokenUtils
 * Descripción: Generador de identificadores únicos y tokens de seguridad.
 * Mejora: Uso de SecureRandom para mayor resistencia ante ataques de predicción.
 */
public class TokenUtils {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * Genera un token numérico de 9 dígitos.
     * Ideal para recuperación de contraseñas por su facilidad de lectura.
     */
    public static String generateNumericToken() {
        // Genera un número entre 100,000,000 y 999,999,999
        int number = 100_000_000 + RANDOM.nextInt(900_000_000);
        return String.valueOf(number);
    }

    /**
     * Genera un token alfanumérico de longitud variable.
     * Usado en AuthService para activación de cuentas (9 caracteres).
     */
    public static String generateAlphanumericToken(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC_CHARS.charAt(RANDOM.nextInt(ALPHANUMERIC_CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * Genera un UUID versión 4 (aleatorio) único.
     * Se usa como identificador público (UuidUser) para no exponer el ID secuencial de la DB.
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
}