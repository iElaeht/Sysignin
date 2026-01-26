package app.dao;

import app.config.ConnectionDB;
import app.models.User;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class UserDAO {

    public boolean registerUser(User user) {
        String sql = "INSERT INTO Users (UuidUser, Username, Password, Email, RegistrationIp, Country, City, State, Token, TokenExpiration) VALUES (?, ?, ?, ?, ?, ?, ?, 'Inactive', ?, DATE_ADD(NOW(), INTERVAL 2 HOUR))";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUuidUser());
            ps.setString(2, user.getUsername());
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
        String sql = "UPDATE Users SET State = 'Active', SecurityPoints = 1, Token = NULL, TokenExpiration = NULL WHERE Email = ? AND Token = ? AND TokenExpiration > NOW()";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, token);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM Users WHERE Email = ? AND isDeleted = false";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public User getUserByUuid(String uuid) {
        String sql = "SELECT * FROM Users WHERE UuidUser = ? AND isDeleted = false";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

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

    public boolean softDelete(String uuid) {
        String sql = "UPDATE Users SET isDeleted = true, State = 'Banned' WHERE UuidUser = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setIdUser(rs.getInt("IdUser"));
        u.setUuidUser(rs.getString("UuidUser"));
        u.setUsername(rs.getString("Username"));
        u.setPassword(rs.getString("Password"));
        u.setEmail(rs.getString("Email"));
        u.setState(rs.getString("State"));
        u.setSecurityPoints(rs.getInt("SecurityPoints"));
        u.setPhoneNumber(rs.getString("PhoneNumber"));
        u.setGender(rs.getString("Gender"));
        return u;
    }
}