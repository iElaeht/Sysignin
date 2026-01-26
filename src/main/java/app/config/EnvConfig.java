package app.config;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvConfig {
    private static final Dotenv dotenv = Dotenv.load();

    /**
     * Obtiene una variable de entorno por su clave.
     * @param key Nombre de la variable en el archivo .env
     * @return El valor de la variable
     */
    public static String get(String key) {
        return dotenv.get(key);
    }
}