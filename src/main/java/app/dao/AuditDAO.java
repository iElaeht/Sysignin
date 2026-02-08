package app.dao;

import app.config.ConnectionDB;
import app.models.AuditLog;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;  
import java.util.List;

/**
 * DAO: AuditDAO
 * Categoría: Seguridad y Monitoreo
 * Descripción: Gestiona el historial de acciones y protección contra fuerza bruta.
 */
public class AuditDAO {

    // ======================================================
    // 1. REGISTRO DE EVENTOS (MODIFICADO)
    // ======================================================

    /**
     * Inserta un nuevo registro de auditoría incluyendo Email y Ubicación.
     */
    public void insertLog(Integer idUser, String identifier, String email, String action, String ip, String location, String userAgent, String details) {
        // Añadimos UserEmail y Location a la consulta SQL
        String sql = "INSERT INTO AuditLogs (IdUser, UserIdentifier, UserEmail, Action, IpSource, Location, UserAgent, Details) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            if (idUser != null) {
                ps.setInt(1, idUser); 
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            
            ps.setString(2, (identifier != null) ? identifier : "UNKNOWN");
            ps.setString(3, email);    // Nueva columna: Email
            ps.setString(4, action);
            ps.setString(5, ip);
            ps.setString(6, location); // Nueva columna: Ubicación (Ciudad, País)
            ps.setString(7, userAgent);
            ps.setString(8, details);
            
            ps.executeUpdate();
            
        } catch (SQLException e) { 
            System.err.println(">>> [AuditDAO] Error al insertar log: " + e.getMessage());
        }
    }

    // ======================================================
    // 2. CONSULTAS DE SEGURIDAD (SIN CAMBIOS)
    // ======================================================

    public int countRecentFailures(String identifier, String action, int minutes) {
        String sql = "SELECT COUNT(*) FROM AuditLogs WHERE UserIdentifier = ? AND Action = ? "
                   + "AND CreatedAt > DATE_SUB(NOW(), INTERVAL ? MINUTE)";
        
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, identifier);
            ps.setString(2, action);
            ps.setInt(3, minutes);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return 0;
    }

    // ======================================================
    // 3. RECUPERACIÓN DE HISTORIAL (MODIFICADO PARA MAPEO MODERNO)
    // ======================================================

    public List<AuditLog> getLogsByUser(String identifier) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM AuditLogs WHERE UserIdentifier = ? ORDER BY CreatedAt DESC LIMIT 100";
        
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) { 
                    list.add(mapAudit(rs)); 
                }
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return list;
    }

    public List<AuditLog> getGlobalLogs() {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM AuditLogs ORDER BY CreatedAt DESC LIMIT 200";
        
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapAudit(rs));
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return list;
    }

    // ======================================================
    // 4. MAPEADOR INTERNO (ACTUALIZADO)
    // ======================================================

    private AuditLog mapAudit(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setIdLog(rs.getInt("IdLog"));
        
        int idUser = rs.getInt("IdUser");
        log.setIdUser(rs.wasNull() ? null : idUser);
        
        log.setUserIdentifier(rs.getString("UserIdentifier"));
        log.setUserEmail(rs.getString("UserEmail")); // Mapeo de la nueva columna
        log.setAction(rs.getString("Action"));
        log.setIpSource(rs.getString("IpSource"));
        log.setLocation(rs.getString("Location"));   // Mapeo de la nueva columna
        log.setUserAgent(rs.getString("UserAgent"));
        log.setDetails(rs.getString("Details"));
        
        // Uso de getObject para activar el import de LocalDateTime y evitar el tono gris
        log.setCreatedAt(rs.getObject("CreatedAt", LocalDateTime.class));
        
        return log;
    }
}