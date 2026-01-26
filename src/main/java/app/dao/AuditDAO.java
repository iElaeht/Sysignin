package app.dao;

import app.database.ConnectionDB;
import app.models.AuditLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditDAO {

    // 1. CREATE: Insertar un nuevo log de auditoría
    public boolean insertLog(AuditLog log) {
        String sql = "INSERT INTO AuditLogs (IdUser, UserIdentifier, Action, IpSource, UserAgent, Details) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Si el IdUser es 0 (porque falló el login y no tenemos el ID), mandamos NULL
            if (log.getIdUser() > 0) {
                ps.setInt(1, log.getIdUser());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            
            ps.setString(2, log.getUserIdentifier());
            ps.setString(3, log.getAction());
            ps.setString(4, log.getIpSource());
            ps.setString(5, log.getUserAgent());
            ps.setString(6, log.getDetails());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. READ: Obtener historial de logs de un usuario específico (Uso de List)
    public List<AuditLog> getLogsByUser(String identifier) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM AuditLogs WHERE UserIdentifier = ? ORDER BY CreatedAt DESC";
        
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, identifier);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AuditLog log = new AuditLog();
                log.setIdLog(rs.getInt("IdLog"));
                log.setIdUser(rs.getInt("IdUser"));
                log.setUserIdentifier(rs.getString("UserIdentifier"));
                log.setAction(rs.getString("Action"));
                log.setIpSource(rs.getString("IpSource"));
                log.setUserAgent(rs.getString("UserAgent"));
                log.setDetails(rs.getString("Details"));
                log.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
                logs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
}