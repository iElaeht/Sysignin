package app.dao;

import app.database.ConnectionDB;
import app.models.Notification;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    // 1. CREATE: Enviar una notificación al usuario
    public boolean createNotification(int idUser, String title, String message, String type) {
        String sql = "INSERT INTO Notifications (IdUser, Title, Message, Type) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idUser);
            ps.setString(2, title);
            ps.setString(3, message);
            ps.setString(4, type);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. READ: Obtener todas las notificaciones no leídas de un usuario
    public List<Notification> getUnreadNotifications(int idUser) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM Notifications WHERE IdUser = ? AND IsRead = FALSE ORDER BY CreatedAt DESC";
        
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idUser);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Notification n = new Notification();
                n.setIdNotification(rs.getInt("IdNotification"));
                n.setTitle(rs.getString("Title"));
                n.setMessage(rs.getString("Message"));
                n.setType(rs.getString("Type"));
                n.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
                list.add(n);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 3. UPDATE: Marcar como leída
    public boolean markAsRead(int idNotification) {
        String sql = "UPDATE Notifications SET IsRead = TRUE WHERE IdNotification = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idNotification);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}