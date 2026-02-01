package app.dao;

import app.config.ConnectionDB;
import app.models.Notification;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO: NotificationDAO
 * Descripción: Gestión de persistencia para el sistema de notificaciones.
 */
public class NotificationDAO {

    // ======================================================
    // 1. CREACIÓN
    // ======================================================

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

    // ======================================================
    // 2. CONSULTAS
    // ======================================================

    public List<Notification> getByUser(String userUuid) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM Notifications WHERE UserUuid = ? ORDER BY CreatedAt DESC LIMIT 20";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, userUuid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapNotification(rs));
                }
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return list;
    }

    public int countUnread(String userUuid) {
        String sql = "SELECT COUNT(*) FROM Notifications WHERE UserUuid = ? AND IsRead = false";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, userUuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return 0;
    }

    // ======================================================
    // 3. ACTUALIZACIONES (SEGURIDAD APLICADA)
    // ======================================================

    public boolean markAsRead(int idNotification, String userUuid) {
        // Blindaje: El UserUuid asegura que el usuario solo modifique sus propias notificaciones
        String sql = "UPDATE Notifications SET IsRead = true WHERE IdNotification = ? AND UserUuid = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idNotification);
            ps.setString(2, userUuid);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return false; 
        }
    }

    public boolean markAllAsRead(String userUuid) {
        String sql = "UPDATE Notifications SET IsRead = true WHERE UserUuid = ? AND IsRead = false";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, userUuid);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return false; 
        }
    }

    // ======================================================
    // 4. MAPEADOR (JDBC -> MODEL)
    // ======================================================

    private Notification mapNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setIdNotification(rs.getInt("IdNotification"));
        n.setUserUuid(rs.getString("UserUuid"));
        n.setTitle(rs.getString("Title"));
        n.setMessage(rs.getString("Message"));
        n.setType(rs.getString("Type"));
        n.setRead(rs.getBoolean("IsRead"));
        
        Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) {
            // Esta línea es la que valida la necesidad del import java.time.LocalDateTime
            n.setCreatedAt(ts.toLocalDateTime());
        }
        return n;
    }
}