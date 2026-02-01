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
    // 1. REGISTRO DE EVENTOS (INSERT)
    // ======================================================

    /**
     * Inserta un nuevo registro de auditoría en la base de datos.
     */
    public void insertLog(Integer idUser, String identifier, String action, String ip, String userAgent, String details) {
        String sql = "INSERT INTO AuditLogs (IdUser, UserIdentifier, Action, IpSource, UserAgent, Details) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Manejo de IdUser nulo para acciones de usuarios no autenticados
            if (idUser != null) {
                ps.setInt(1, idUser); 
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            
            ps.setString(2, (identifier != null) ? identifier : "UNKNOWN");
            ps.setString(3, action);
            ps.setString(4, ip);
            ps.setString(5, userAgent);
            ps.setString(6, details);
            
            ps.executeUpdate();
            
        } catch (SQLException e) { 
            System.err.println(">>> [AuditDAO] Error al insertar log: " + e.getMessage());
        }
    }

    // ======================================================
    // 2. CONSULTAS DE SEGURIDAD (ANTI-BRUTE FORCE)
    // ======================================================

    /**
     * Cuenta intentos fallidos en un rango de tiempo específico.
     * @param minutes Cantidad de minutos hacia atrás desde el momento actual.
     */
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
    // 3. RECUPERACIÓN DE HISTORIAL (LISTS)
    // ======================================================

    /**
     * Obtiene los últimos 100 registros de un usuario específico.
     */
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

    /**
     * Obtiene los últimos 200 registros globales para el panel de administración.
     */
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
    // 4. MAPEADOR INTERNO (JDBC TO MODEL)
    // ======================================================

    /**
     * Transfiere los datos del ResultSet al objeto AuditLog.
     * Aquí se realiza la conversión crítica de Timestamp a LocalDateTime.
     */
    private AuditLog mapAudit(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setIdLog(rs.getInt("IdLog"));
        
        // Manejo de valores nulos en IdUser
        int idUser = rs.getInt("IdUser");
        log.setIdUser(rs.wasNull() ? null : idUser);
        
        log.setUserIdentifier(rs.getString("UserIdentifier"));
        log.setAction(rs.getString("Action"));
        log.setIpSource(rs.getString("IpSource"));
        log.setUserAgent(rs.getString("UserAgent"));
        log.setDetails(rs.getString("Details"));
        
        // Conversión de fecha
        Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) {
            // Se usa LocalDateTime explícitamente para validar el import
            LocalDateTime ldt = ts.toLocalDateTime();
            log.setCreatedAt(ldt);
        }
        
        return log;
    }
}