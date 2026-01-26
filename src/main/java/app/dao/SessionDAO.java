package app.dao;

import app.database.ConnectionDB;
import app.models.UserSession;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessionDAO {

    // 1. CREATE: Iniciar una sesión (Aquí guardamos si marcó "Recordar por 30 días")
    public boolean createSession(UserSession session) {
        String sql = "INSERT INTO UserSessions (UserUuid, DeviceInfo, DeviceType, IpAddress, Country, City, IsTrusted) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, session.getUserUuid());
            ps.setString(2, session.getDeviceInfo());
            ps.setString(3, session.getDeviceType());
            ps.setString(4, session.getIpAddress());
            ps.setString(5, session.getCountry());
            ps.setString(6, session.getCity());
            ps.setBoolean(7, session.isTrusted()); // El parámetro de "Confianza"

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. READ: Ver sesiones activas de un usuario
    public List<UserSession> getActiveSessions(String userUuid) {
        List<UserSession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM UserSessions WHERE UserUuid = ? AND IsActive = TRUE";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, userUuid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UserSession s = new UserSession();
                s.setIdSession(rs.getInt("IdSession"));
                s.setDeviceInfo(rs.getString("DeviceInfo"));
                s.setIpAddress(rs.getString("IpAddress"));
                s.setTrusted(rs.getBoolean("IsTrusted"));
                s.setLastActivity(rs.getTimestamp("LastActivity").toLocalDateTime());
                sessions.add(s);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return sessions;
    }

    // 3. DELETE (Log Out): Cerrar una sesión específica
    public boolean closeSession(int idSession) {
        String sql = "UPDATE UserSessions SET IsActive = FALSE WHERE IdSession = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSession);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}