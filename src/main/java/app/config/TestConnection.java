package app.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestConnection {
    public static void main(String[] args) {
        System.out.println("=== INICIANDO TEST DE CONEXIÓN ===");
        
        try {
            // 1. Probar lectura de .env
            String dbUrl = EnvConfig.get("DB_URL");
            System.out.println("1. Intentando conectar a: " + dbUrl);

            // 2. Intentar obtener la conexión
            Connection conn = ConnectionDB.getConnection();
            
            if (conn != null && !conn.isClosed()) {
                System.out.println("2. ¡ÉXITO! Conexión establecida con MySQL.");
                
                // 3. Prueba real: Consultar la versión de la base de datos
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT VERSION()");
                
                if (rs.next()) {
                    System.out.println("3. Versión de MySQL: " + rs.getString(1));
                    System.out.println("4. Base de datos actual: " + conn.getCatalog());
                }
                
                // 4. Cerrar prueba
                ConnectionDB.closeConnection();
                System.out.println("=== TEST FINALIZADO CON ÉXITO ===");
            }
            
        } catch (Exception e) {
            System.err.println("❌ ERROR EN EL TEST:");
            System.err.println("Causa: " + e.getMessage());
            e.printStackTrace();
        }
    }
}