package app.dao;

import app.config.ConnectionDB;
import app.models.AuditLog;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;  
import java.util.List;          

public class AuditDAO {

    // 1. Insertar Log: Captura acciones de seguridad
    public void insertLog(Integer idUser, String identifier, String action, String ip, String userAgent, String details) {
        String sql = "INSERT INTO AuditLogs (IdUser, UserIdentifier, Action, IpSource, UserAgent, Details) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            if (idUser != null) ps.setInt(1, idUser); 
            else ps.setNull(1, Types.INTEGER);
            
            ps.setString(2, (identifier != null) ? identifier : "UNKNOWN");
            ps.setString(3, action);
            ps.setString(4, ip);
            ps.setString(5, userAgent);
            ps.setString(6, details);
            
            ps.executeUpdate();
        } catch (SQLException e) { 
            System.err.println("Error al insertar log de auditoría: " + e.getMessage());
        }
    }

    // 2. Obtener historial (Usa List y ArrayList)
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
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 3. Obtener auditoría global para Admin
    public List<AuditLog> getGlobalLogs() {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM AuditLogs ORDER BY CreatedAt DESC LIMIT 200";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapAudit(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 4. Sistema Anti-Fuerza Bruta: INTERVALO DINÁMICO
    public int countRecentFailures(String identifier, String action, int minutes) {
        String sql = "SELECT COUNT(*) FROM AuditLogs WHERE UserIdentifier = ? AND Action = ? AND CreatedAt > DATE_SUB(NOW(), INTERVAL ? MINUTE)";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, action);
            ps.setInt(3, minutes);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // 5. Mapeador (Uso explícito de LocalDateTime)
    private AuditLog mapAudit(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setIdLog(rs.getInt("IdLog"));
        
        int idUser = rs.getInt("IdUser");
        log.setIdUser(rs.wasNull() ? null : idUser);
        
        log.setUserIdentifier(rs.getString("UserIdentifier"));
        log.setAction(rs.getString("Action"));
        log.setIpSource(rs.getString("IpSource"));
        log.setUserAgent(rs.getString("UserAgent"));
        log.setDetails(rs.getString("Details"));
        
        Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) {
            LocalDateTime createdAt = ts.toLocalDateTime();
            log.setCreatedAt(createdAt);
        }
        
        return log;
    }
}