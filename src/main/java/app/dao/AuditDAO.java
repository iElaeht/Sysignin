package app.dao;

import app.config.ConnectionDB;
import app.models.AuditLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditDAO {

    public void insertLog(Integer idUser, String identifier, String action, String ip, String userAgent, String details) {
        String sql = "INSERT INTO AuditLogs (IdUser, UserIdentifier, Action, IpSource, UserAgent, Details, CreatedAt) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (idUser != null) ps.setInt(1, idUser); else ps.setNull(1, Types.INTEGER);
            ps.setString(2, identifier);
            ps.setString(3, action);
            ps.setString(4, ip);
            ps.setString(5, userAgent);
            ps.setString(6, details);
            ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<AuditLog> getLogsByUser(String identifier) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM AuditLogs WHERE UserIdentifier = ? ORDER BY CreatedAt DESC LIMIT 50";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapAudit(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int countRecentFailures(String ip, String action) {
        String sql = "SELECT COUNT(*) FROM AuditLogs WHERE IpSource = ? AND Action = ? AND CreatedAt > DATE_SUB(NOW(), INTERVAL 1 HOUR)";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ip);
            ps.setString(2, action);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

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
        if (ts != null) log.setCreatedAt(ts.toLocalDateTime());
        return log;
    }
}