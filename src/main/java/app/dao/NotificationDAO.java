package app.dao;

import app.config.ConnectionDB;
import app.models.Notification;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public void createNotification(String userUuid, String title, String message, String type) {
        String sql = "INSERT INTO Notifications (UserUuid, Title, Message, Type, CreatedAt) VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userUuid);
            ps.setString(2, title);
            ps.setString(3, message);
            ps.setString(4, type);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Notification> getByUser(String userUuid) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM Notifications WHERE UserUuid = ? ORDER BY CreatedAt DESC LIMIT 10";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userUuid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Notification n = new Notification();
                n.setIdNotification(rs.getInt("IdNotification"));
                n.setTitle(rs.getString("Title"));
                n.setMessage(rs.getString("Message"));
                n.setType(rs.getString("Type"));
                list.add(n);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}