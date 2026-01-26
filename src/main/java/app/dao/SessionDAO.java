package app.dao;

import app.config.ConnectionDB;
import app.models.UserSession;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessionDAO {

    public void createSession(UserSession session) {
        String sql = "INSERT INTO UserSessions (UserUuid, DeviceInfo, DeviceType, IpAddress, Country, City, IsActive, LoginTime) VALUES (?, ?, ?, ?, ?, ?, true, NOW())";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, session.getUserUuid());
            ps.setString(2, session.getDeviceInfo());
            ps.setString(3, session.getDeviceType());
            ps.setString(4, session.getIpAddress());
            ps.setString(5, session.getCountry());
            ps.setString(6, session.getCity());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<UserSession> getActiveSessions(String userUuid) {
        List<UserSession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM UserSessions WHERE UserUuid = ? AND IsActive = true ORDER BY LoginTime DESC";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userUuid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { sessions.add(mapSession(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return sessions;
    }

    public boolean terminateSession(int idSession) {
        String sql = "UPDATE UserSessions SET IsActive = false WHERE IdSession = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSession);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private UserSession mapSession(ResultSet rs) throws SQLException {
        UserSession s = new UserSession();
        s.setIdSession(rs.getInt("IdSession"));
        s.setUserUuid(rs.getString("UserUuid"));
        s.setDeviceInfo(rs.getString("DeviceInfo"));
        s.setIpAddress(rs.getString("IpAddress"));
        s.setCountry(rs.getString("Country"));
        Timestamp ts = rs.getTimestamp("LoginTime");
        if (ts != null) s.setLoginTime(ts.toLocalDateTime());
        return s;
    }
}