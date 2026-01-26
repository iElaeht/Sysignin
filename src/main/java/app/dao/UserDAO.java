package app.dao;

import app.database.ConnectionDB;
import app.models.User;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // 1. CREATE: Registrar usuario
    public boolean registerUser(User user) {
        String sql = "INSERT INTO Users (UuidUser, Username, Password, Email, RegistrationIp, State, Token) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, user.getUuidUser());
            ps.setString(2, user.getUsername());
            ps.setString(3, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRegistrationIp());
            ps.setString(6, "Inactive");
            ps.setString(7, user.getToken());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. READ (Individual): Login / Buscar por Email
    public User login(String email, String password) {
        String sql = "SELECT * FROM Users WHERE Email = ? AND IsDeleted = FALSE";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String hashed = rs.getString("Password");
                if (BCrypt.checkpw(password, hashed)) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 3. READ (Lista): Listar todos los usuarios (Aquí es donde usamos List y ArrayList)
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM Users WHERE IsDeleted = FALSE";
        
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                // Usamos nuestro método de mapeo para llenar la lista
                userList.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    // 4. READ (Individual): Buscar por UUID
    public User getUserByUuid(String uuid) {
        String sql = "SELECT * FROM Users WHERE UuidUser = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSetToUser(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // 5. UPDATE: Actualizar perfil
    public boolean updateUser(User user) {
        String sql = "UPDATE Users SET PhoneNumber = ?, Country = ?, City = ?, PreferredTheme = ?, Languages = ? WHERE UuidUser = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getPhoneNumber());
            ps.setString(2, user.getCountry());
            ps.setString(3, user.getCity());
            ps.setString(4, user.getPreferredTheme());
            ps.setString(5, user.getLanguages());
            ps.setString(6, user.getUuidUser());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 6. DELETE: Borrado lógico
    public boolean softDeleteUser(String uuid) {
        String sql = "UPDATE Users SET IsDeleted = TRUE, State = 'Inactive' WHERE UuidUser = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 7. UTILS: Verificar Email
    public boolean isEmailTaken(String email) {
        String sql = "SELECT COUNT(*) FROM Users WHERE Email = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // Método de mapeo (Privado para uso interno)
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setIdUser(rs.getInt("IdUser"));
        u.setUuidUser(rs.getString("UuidUser"));
        u.setUsername(rs.getString("Username"));
        u.setEmail(rs.getString("Email"));
        u.setRoles(rs.getString("Roles"));
        u.setState(rs.getString("State"));
        u.setCountry(rs.getString("Country"));
        u.setCity(rs.getString("City"));
        u.setTwoFactorEnabled(rs.getBoolean("TwoFactorEnabled"));
        return u;
    }
}