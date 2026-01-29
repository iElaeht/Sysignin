package app.dao;

import app.config.ConnectionDB;
import app.models.User;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // --- 1. REGISTRO Y SEGURIDAD DE ACCESO ---

    public boolean registerUser(User user) {
        String sql = "INSERT INTO Users (UuidUser, Username, Password, Email, RegistrationIp, Country, City, Token, State, TokenExpiration, TokenAttempts) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'Inactive', ?, 0)";
        
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUuidUser());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRegistrationIp());
            ps.setString(6, user.getCountry());
            ps.setString(7, user.getCity());
            ps.setString(8, user.getToken());
            ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now().plusMinutes(15)));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean activateAccount(String email, String token) {
        String sql = "UPDATE Users SET State = 'Active', Token = NULL, TokenExpiration = NULL, TokenAttempts = 0 " +
                     "WHERE Email = ? AND Token = ? AND TokenExpiration > NOW()";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, token);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public void incrementTokenAttempts(String email) {
        String sql = "UPDATE Users SET TokenAttempts = TokenAttempts + 1 WHERE Email = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- 2. GESTIÓN DE SESIÓN Y LOGS ---

    public void updateLastIp(String uuid, String ip) {
        String sql = "UPDATE Users SET LastIp = ?, LastLogin = NOW() WHERE UuidUser = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ip);
            ps.setString(2, uuid);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- 3. CRUD COMPLETO (Listar, Buscar, Actualizar, Eliminar) ---

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM Users WHERE IsDeleted = false ORDER BY DateRegistration DESC";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { list.add(mapUser(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public User getUserByEmail(String identifier) {
        // Buscamos por Email o Username (muy útil para el login)
        String sql = "SELECT * FROM Users WHERE (Email = ? OR Username = ?) AND IsDeleted = false";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
    public boolean updateProfile(User user) {
        String sql = "UPDATE Users SET Username = ?, PhoneNumber = ?, Gender = ?, Country = ?, City = ? WHERE UuidUser = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPhoneNumber());
            ps.setString(3, user.getGender());
            ps.setString(4, user.getCountry());
            ps.setString(5, user.getCity());
            ps.setString(6, user.getUuidUser());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean changeState(String uuid, String newState) {
        // Esto permite al admin cambiar a 'Active', 'Ban', 'Suspicious', etc.
        String sql = "UPDATE Users SET State = ? WHERE UuidUser = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newState);
            ps.setString(2, uuid);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean softDelete(String uuid) {
        // Eliminación lógica: no borramos el dato, lo ocultamos
        String sql = "UPDATE Users SET IsDeleted = true WHERE UuidUser = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // --- 4. MÉTODO DE LIMPIEZA AUTOMÁTICA (Para tu idea) ---

    public int cleanInactiveAccounts(int hours) {
        // Borra solo si el estado es Inactive (no Ban ni Active) y pasó el tiempo
        String sql = "DELETE FROM Users WHERE State = 'Inactive' AND DateRegistration < DATE_SUB(NOW(), INTERVAL ? HOUR)";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hours);
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }
    public boolean updateToken(String email, String token) {
        String sql = "UPDATE Users SET Token = ?, TokenExpiration = ?, TokenAttempts = 0 WHERE Email = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            // El token vive 15 min, pero el Service controlará el cooldown de 30s
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now().plusMinutes(15)));
            ps.setString(3, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public void resetAttempts(String email) {
        String sql = "UPDATE Users SET TokenAttempts = 0 WHERE Email = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    public void applyPenalty(String email, int minutes) {
        String sql = "UPDATE Users SET PenaltyTime = ? WHERE Email = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now().plusMinutes(minutes)));
            ps.setString(2, email);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- 5. MAPEADOR ---

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setIdUser(rs.getInt("IdUser"));
        u.setUuidUser(rs.getString("UuidUser"));
        u.setUsername(rs.getString("Username"));
        u.setPassword(rs.getString("Password"));
        u.setEmail(rs.getString("Email"));
        u.setBackupEmail(rs.getString("BackupEmail"));
        u.setPhoneNumber(rs.getString("PhoneNumber"));
        u.setGender(rs.getString("Gender"));
        u.setRoles(rs.getString("Roles"));
        u.setState(rs.getString("State"));
        u.setToken(rs.getString("Token"));
        u.setTokenAttempts(rs.getInt("TokenAttempts"));
        u.setRegistrationIp(rs.getString("RegistrationIp"));
        u.setLastIp(rs.getString("LastIp"));
        u.setCountry(rs.getString("Country"));
        u.setCity(rs.getString("City"));

        Timestamp tsExp = rs.getTimestamp("TokenExpiration");
        if (tsExp != null) u.setTokenExpiration(tsExp.toLocalDateTime());
        
        Timestamp tsReg = rs.getTimestamp("DateRegistration");
        if (tsReg != null) u.setDateRegistration(tsReg.toLocalDateTime());

        Timestamp tsLogin = rs.getTimestamp("LastLogin");
        if (tsLogin != null) u.setLastLogin(tsLogin.toLocalDateTime());
        
        return u;
    }
}