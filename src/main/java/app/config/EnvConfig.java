package app.config;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvConfig {
    // Configuración optimizada para aplicaciones Web (Tomcat)
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load();

    public static String get(String key) {
        // Primero intentamos obtener de Dotenv
        String value = dotenv.get(key);
        
        // Si no está en .env, intentamos buscar en variables de entorno del Sistema
        // (Esto es útil para servidores de producción)
        if (value == null || value.isEmpty()) {
            value = System.getenv(key);
        }
        
        return value;
    }
}