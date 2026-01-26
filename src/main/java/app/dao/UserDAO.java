package app.dao;

import app.config.ConnectionDB;
import app.models.User;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class UserDAO {

    // --- REGISTRO Y ACTIVACIÓN ---

    public boolean registerUser(User user) {
        // Sincronizado con tu SQL: incluye Uuid, Username, Password, Email, Ip, Country, City, Token y Expiración
        String sql = "INSERT INTO Users (UuidUser, Username, Password, Email, RegistrationIp, Country, City, State, Token, TokenExpiration) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, 'Inactive', ?, DATE_ADD(NOW(), INTERVAL 2 HOUR))";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUuidUser());
            ps.setString(2, user.getUsername());
            // El hash se genera aquí para asegurar que NUNCA viaje texto plano a la DB
            ps.setString(3, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12)));
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRegistrationIp());
            ps.setString(6, user.getCountry());
            ps.setString(7, user.getCity());
            ps.setString(8, user.getToken());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean activateAccount(String email, String token) {
        // Sincronizado: Activa cuenta y otorga el primer SecurityPoint
        String sql = "UPDATE Users SET State = 'Active', Token = NULL, TokenExpiration = NULL WHERE Email = ? AND Token = ? AND TokenExpiration > NOW()";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, token);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // --- BÚSQUEDAS (READ) ---

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM Users WHERE Email = ? AND IsDeleted = false";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public User getUserByUuid(String uuid) {
        String sql = "SELECT * FROM Users WHERE UuidUser = ? AND IsDeleted = false";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // --- ACTUALIZACIONES DE PERFIL Y SEGURIDAD (UPDATE) ---

    public boolean updateProfile(User user) {
        String sql = "UPDATE Users SET Username = ?, PhoneNumber = ?, Gender = ? WHERE UuidUser = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPhoneNumber());
            ps.setString(3, user.getGender());
            ps.setString(4, user.getUuidUser());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // Sincronización: Actualización de Contraseña tras validar Token
    public boolean updatePassword(String uuid, String newPassword) {
        String sql = "UPDATE Users SET Password = ? WHERE UuidUser = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));
            ps.setString(2, uuid);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // Sincronización: Actualización de Email Principal tras doble validación
    public boolean updateEmail(String uuid, String newEmail) {
        String sql = "UPDATE Users SET Email = ? WHERE UuidUser = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newEmail);
            ps.setString(2, uuid);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // Sincronización: Actualización de Correo de Recuperación
    public boolean updateBackupEmail(String uuid, String backupEmail) {
        String sql = "UPDATE Users SET BackupEmail = ? WHERE UuidUser = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, backupEmail);
            ps.setString(2, uuid);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // --- BORRADO LÓGICO ---

    public boolean softDelete(String uuid) {
        String sql = "UPDATE Users SET IsDeleted = true, State = 'Inactive' WHERE UuidUser = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    // --- MÉTODOS PARA ADMINISTRACIÓN (NUEVOS) ---

    // 1. Obtener todos los usuarios para el panel de administración
    public java.util.List<User> getAllUsers() {
        java.util.List<User> list = new java.util.ArrayList<>();
        // Traemos a todos los que no han sido borrados definitivamente
        String sql = "SELECT * FROM Users WHERE IsDeleted = false ORDER BY CreatedAt DESC";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Actualizar estado (Banned, Active, Inactive) desde el Admin
    public boolean updateStatus(String uuid, String newStatus) {
        // Sincronizado con el ENUM 'State' de tu base de datos
        String sql = "UPDATE Users SET State = ? WHERE UuidUser = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, uuid);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- MAPEADOR ---

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setIdUser(rs.getInt("IdUser"));
        u.setUuidUser(rs.getString("UuidUser"));
        u.setUsername(rs.getString("Username"));
        u.setPassword(rs.getString("Password"));
        u.setEmail(rs.getString("Email"));
        u.setBackupEmail(rs.getString("BackupEmail")); // Sincronizado
        u.setState(rs.getString("State"));
        u.setPhoneNumber(rs.getString("PhoneNumber"));
        u.setGender(rs.getString("Gender"));
        u.setRoles(rs.getString("Roles")); // Sincronizado con ENUM
        return u;
    }
}