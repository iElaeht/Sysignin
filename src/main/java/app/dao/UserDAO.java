package app.dao;

import app.config.ConnectionDB;
import app.models.User;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO: UserDAO
 * Estado: Optimizado y Limpio con Retorno de ID
 */
public class UserDAO {

    // ======================================================
    // 1. REGISTRO Y ACTIVACIÓN
    // ======================================================

    public int registerUser(User user) {
        String sql = "INSERT INTO Users (UuidUser, Username, Password, Email, RegistrationIp, Country, City, Token, State, TokenExpiration, TokenAttempts, LoginAttempts) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'Inactive', DATE_ADD(NOW(), INTERVAL 15 MINUTE), 0, 0)";
        
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, user.getUuidUser());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRegistrationIp());
            ps.setString(6, user.getCountry());
            ps.setString(7, user.getCity());
            ps.setString(8, user.getToken());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // Retorna el ID generado
                    }
                }
            }
            return 0;
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return 0; 
        }
    }

    public boolean activateAccount(String email, String token) {
        String sql = "UPDATE Users SET State = 'Active', Token = NULL, TokenExpiration = NULL, TokenAttempts = 0 " +
                     "WHERE Email = ? AND Token = ? AND TokenExpiration > NOW() " + 
                     "AND (PenaltyTime IS NULL OR PenaltyTime < NOW())";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, token);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return false; 
        }
    }

    public boolean updateToken(String email, String token) {
        String sql = "UPDATE Users SET Token = ?, TokenExpiration = DATE_ADD(NOW(), INTERVAL 10 MINUTE), TokenAttempts = 0 " + 
                     "WHERE Email = ? AND (PenaltyTime IS NULL OR PenaltyTime < NOW()) AND IsDeleted = false";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return false; 
        }
    }

    // ======================================================
    // 2. SEGURIDAD Y CONTROL DE INTENTOS
    // ======================================================

    public void incrementTokenAttempts(String email) {
        String sql = "UPDATE Users SET TokenAttempts = LEAST(TokenAttempts + 1, 5) WHERE Email = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void incrementLoginAttempts(String email) {
        String sql = "UPDATE Users SET LoginAttempts = LoginAttempts + 1 WHERE Email = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void applyPenalty(String email, int minutes) {
        String sql = "UPDATE Users SET PenaltyTime = DATE_ADD(NOW(), INTERVAL ? MINUTE), TokenAttempts = 5 WHERE Email = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, minutes);
            ps.setString(2, email);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void resetAttempts(String email) {
        String sql = "UPDATE Users SET TokenAttempts = 0, LoginAttempts = 0, PenaltyTime = NULL WHERE Email = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ======================================================
    // 3. BÚSQUEDAS Y CONSULTAS
    // ======================================================

    public User getUserByIdentifier(String identifier) {
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

    public User getUserByUuid(String uuid) {
        String sql = "SELECT * FROM Users WHERE UuidUser = ? AND IsDeleted = false";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM Users WHERE IsDeleted = false ORDER BY DateRegistration DESC";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { 
                list.add(mapUser(rs)); 
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ======================================================
    // 4. ACTUALIZACIONES DE PERFIL
    // ======================================================

    public boolean updatePassword(String uuid, String newHashedPassword) {
        String sql = "UPDATE Users SET Password = ? WHERE UuidUser = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHashedPassword);
            ps.setString(2, uuid);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateEmail(String uuid, String newEmail) {
        String sql = "UPDATE Users SET Email = ? WHERE UuidUser = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newEmail);
            ps.setString(2, uuid);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
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

    public void updateLastIp(String uuid, String ip) {
        String sql = "UPDATE Users SET LastIp = ?, LastLogin = NOW() WHERE UuidUser = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ip);
            ps.setString(2, uuid);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ======================================================
    // 5. ADMINISTRACIÓN Y MANTENIMIENTO
    // ======================================================

    public boolean changeState(String uuid, String newState) {
        String sql = "UPDATE Users SET State = ? WHERE UuidUser = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newState);
            ps.setString(2, uuid);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean softDelete(String uuid) {
        String sql = "UPDATE Users SET IsDeleted = true WHERE UuidUser = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public int cleanInactiveAccounts(int hours) {
        String sql = "DELETE FROM Users WHERE State = 'Inactive' AND DateRegistration < DATE_SUB(NOW(), INTERVAL ? HOUR)";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hours);
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }
    public void updateRegistrationIp(String email, String newIp) {
        // CAMBIO: Debe ser RegistrationIp (con R y I mayúsculas) para coincidir con tu tabla
        String sql = "UPDATE Users SET RegistrationIp = ? WHERE Email = ?";
        
        try (Connection conn = ConnectionDB.getConnection(); 
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, newIp);
            ps.setString(2, email);
            ps.executeUpdate();
            
            System.out.println("LOG: IP de registro actualizada a " + newIp + " para " + email);
            
        } catch (SQLException e) {
            System.err.println("ERROR en updateRegistrationIp: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // ======================================================
    // 6. MAPEADOR (JDBC -> MODEL)
    // ======================================================

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
            u.setLoginAttempts(rs.getInt("LoginAttempts"));
            u.setRegistrationIp(rs.getString("RegistrationIp"));
            u.setLastIp(rs.getString("LastIp"));
            u.setCountry(rs.getString("Country"));
            u.setCity(rs.getString("City"));
            u.setSocialId(rs.getString("SocialId"));
            u.setAuthProvider(rs.getString("AuthProvider"));

            // Esto activa el import y maneja nulos automáticamente
            u.setPenaltyTime(rs.getObject("PenaltyTime", LocalDateTime.class));
            u.setTokenExpiration(rs.getObject("TokenExpiration", LocalDateTime.class));
            u.setDateRegistration(rs.getObject("DateRegistration", LocalDateTime.class));
            u.setLastLogin(rs.getObject("LastLogin", LocalDateTime.class));
            
            return u;
        }
}