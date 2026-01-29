package app.dao;

import app.config.ConnectionDB;
import app.models.UserSession;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionDAO {

    // 1. Crear Sesión: Registra el inicio de sesión
    public void createSession(UserSession session) {
        String sql = "INSERT INTO UserSessions (UserUuid, DeviceInfo, DeviceType, IpAddress, Country, City, IsActive) VALUES (?, ?, ?, ?, ?, ?, true)";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, session.getUserUuid());
            ps.setString(2, session.getDeviceInfo());
            ps.setString(3, session.getDeviceType());
            ps.setString(4, session.getIpAddress());
            ps.setString(5, session.getCountry());
            ps.setString(6, session.getCity());
            ps.executeUpdate();
        } catch (SQLException e) { 
            System.err.println("Error al registrar sesión: " + e.getMessage());
        }
    }

    // 2. Obtener Sesiones Activas (Usa List, ArrayList y ResultSet en try)
    public List<UserSession> getActiveSessions(String userUuid) {
        List<UserSession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM UserSessions WHERE UserUuid = ? AND IsActive = true ORDER BY LastActivity DESC";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userUuid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) { 
                    sessions.add(mapSession(rs)); 
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return sessions;
    }

    // 3. Actualizar Actividad: Mantiene la sesión viva
    public void updateActivity(int idSession) {
        String sql = "UPDATE UserSessions SET LastActivity = NOW() WHERE IdSession = ? AND IsActive = true";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSession);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 4. Terminar Sesión Específica
    public boolean terminateSession(int idSession, String userUuid) {
        String sql = "UPDATE UserSessions SET IsActive = false WHERE IdSession = ? AND UserUuid = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSession);
            ps.setString(2, userUuid);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 5. NUEVO: Cerrar todas las sesiones excepto la actual (Seguridad proactiva)
    public boolean terminateOtherSessions(String userUuid, int currentSessionId) {
        String sql = "UPDATE UserSessions SET IsActive = false WHERE UserUuid = ? AND IdSession <> ? AND IsActive = true";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userUuid);
            ps.setInt(2, currentSessionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    private UserSession mapSession(ResultSet rs) throws SQLException {
        UserSession s = new UserSession();
        s.setIdSession(rs.getInt("IdSession"));
        s.setUserUuid(rs.getString("UserUuid"));
        s.setDeviceInfo(rs.getString("DeviceInfo"));
        s.setDeviceType(rs.getString("DeviceType"));
        s.setIpAddress(rs.getString("IpAddress"));
        s.setCountry(rs.getString("Country"));
        s.setCity(rs.getString("City"));
        s.setTrusted(rs.getBoolean("IsTrusted"));
        s.setActive(rs.getBoolean("IsActive"));
        
        Timestamp loginTs = rs.getTimestamp("LoginTime");
        if (loginTs != null) {
            LocalDateTime loginLdt = loginTs.toLocalDateTime();
            s.setLoginTime(loginLdt);
        }
        
        Timestamp activityTs = rs.getTimestamp("LastActivity");
        if (activityTs != null) {
            LocalDateTime activityLdt = activityTs.toLocalDateTime();
            s.setLastActivity(activityLdt);
        }
        
        return s;
    }
}