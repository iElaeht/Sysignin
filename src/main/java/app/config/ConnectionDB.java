package app.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionDB {
    
    private static Connection connection = null;
    public static Connection getConnection() throws SQLException {
        // Si la conexión no existe o se cerró, la creamos
        if (connection == null || connection.isClosed()) {
            try {
                // Obtenemos los datos desde EnvConfig
                String url = EnvConfig.get("DB_URL");
                String user = EnvConfig.get("DB_USER");
                String pass = EnvConfig.get("DB_PASSWORD");
                
                // Cargamos el Driver de MySQL (indispensable en entornos Web)
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                connection = DriverManager.getConnection(url, user, pass);
                System.out.println("LOG: Conexión establecida exitosamente.");
            } catch (ClassNotFoundException e) {
                System.err.println("ERROR: Driver MySQL no encontrado.");
                throw new SQLException(e);
            }
        }
        return connection;
    }
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("LOG: Conexión cerrada.");
            } catch (SQLException e) {
                System.err.println("ERROR al cerrar conexión: " + e.getMessage());
            }
        }
    }
}