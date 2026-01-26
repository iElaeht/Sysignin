package app.dao;

import app.config.ConnectionDB;
import app.models.Notification;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    // 1. Crear Notificación: Sincronizado con los tipos ENUM ('Security', 'System', 'Social', 'Alert')
    public void createNotification(String userUuid, String title, String message, String type) {
        String sql = "INSERT INTO Notifications (UserUuid, Title, Message, Type) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userUuid);
            ps.setString(2, title);
            ps.setString(3, message);
            ps.setString(4, type);
            ps.executeUpdate();
        } catch (SQLException e) { 
            System.err.println("Error al crear notificación: " + e.getMessage());
        }
    }

    // 2. Obtener las últimas notificaciones del usuario (Sincronizado con IsRead)
    public List<Notification> getByUser(String userUuid) {
        List<Notification> list = new ArrayList<>();
        // Traemos las 20 más recientes para que el usuario tenga historial
        String sql = "SELECT * FROM Notifications WHERE UserUuid = ? ORDER BY CreatedAt DESC LIMIT 20";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userUuid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapNotification(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 3. Sincronización: Marcar notificación como leída
    public boolean markAsRead(int idNotification) {
        String sql = "UPDATE Notifications SET IsRead = true WHERE IdNotification = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idNotification);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 4. Sincronización: Contar notificaciones no leídas (Para el "badge" o globito rojo en la UI)
    public int countUnread(String userUuid) {
        String sql = "SELECT COUNT(*) FROM Notifications WHERE UserUuid = ? AND IsRead = false";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userUuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // 5. Mapeador privado
    private Notification mapNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setIdNotification(rs.getInt("IdNotification"));
        n.setUserUuid(rs.getString("UserUuid"));
        n.setTitle(rs.getString("Title"));
        n.setMessage(rs.getString("Message"));
        n.setType(rs.getString("Type"));
        n.setRead(rs.getBoolean("IsRead"));
        Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) n.setCreatedAt(ts.toLocalDateTime());
        return n;
    }
}