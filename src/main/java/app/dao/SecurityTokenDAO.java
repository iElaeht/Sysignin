package app.dao;

import app.config.ConnectionDB;
import app.models.SecurityToken;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO: SecurityTokenDAO
 * Categoría: Seguridad y Autenticación
 */
public class SecurityTokenDAO {

    // ======================================================
    // 1. OPERACIONES DE ESCRITURA
    // ======================================================

    public boolean insertToken(SecurityToken st) {
        String sql = "INSERT INTO SecurityTokens (UserUuid, TokenType, TokenCode, NewValue, ExpiresAt) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, st.getUserUuid());
            ps.setString(2, st.getTokenType());
            ps.setString(3, st.getTokenCode());
            ps.setString(4, st.getNewValue());
            
            // INTEGRACIÓN 1: Conversión de LocalDateTime a Timestamp para MySQL
            LocalDateTime expiration = st.getExpiresAt();
            ps.setTimestamp(5, Timestamp.valueOf(expiration));
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ======================================================
    // 2. VALIDACIÓN Y SEGURIDAD
    // ======================================================

    public SecurityToken validateToken(String uuid, String code, String type) {
        String sql = "SELECT * FROM SecurityTokens WHERE UserUuid = ? AND TokenCode = ? AND TokenType = ? " +
                     "AND IsUsed = FALSE AND ExpiresAt > NOW()";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, uuid);
            ps.setString(2, code);
            ps.setString(3, type);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapSecurityToken(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean markAsUsed(int idToken) {
        String sql = "UPDATE SecurityTokens SET IsUsed = TRUE WHERE IdToken = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idToken);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ======================================================
    // 3. CONSULTAS Y AUDITORÍA
    // ======================================================

    public SecurityToken getLastToken(String uuid, String type) {
        String sql = "SELECT * FROM SecurityTokens WHERE UserUuid = ? AND TokenType = ? " +
                     "ORDER BY CreatedAt DESC LIMIT 1";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, uuid);
            ps.setString(2, type);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapSecurityToken(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<SecurityToken> getTokensByUser(String uuid) {
        List<SecurityToken> list = new ArrayList<>();
        String sql = "SELECT * FROM SecurityTokens WHERE UserUuid = ? ORDER BY CreatedAt DESC LIMIT 100";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSecurityToken(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ======================================================
    // 4. MAPEADOR (JDBC -> MODEL)
    // ======================================================

    private SecurityToken mapSecurityToken(ResultSet rs) throws SQLException {
        SecurityToken st = new SecurityToken();
        st.setIdToken(rs.getInt("IdToken"));
        st.setUserUuid(rs.getString("UserUuid"));
        st.setTokenType(rs.getString("TokenType"));
        st.setTokenCode(rs.getString("TokenCode"));
        st.setNewValue(rs.getString("NewValue"));
        st.setUsed(rs.getBoolean("IsUsed"));
        
        // INTEGRACIÓN 2: Mapeo explícito usando la clase LocalDateTime
        Timestamp createdTs = rs.getTimestamp("CreatedAt");
        if (createdTs != null) {
            LocalDateTime createdLdt = createdTs.toLocalDateTime();
            st.setCreatedAt(createdLdt);
        }
        
        Timestamp expiresTs = rs.getTimestamp("ExpiresAt");
        if (expiresTs != null) {
            LocalDateTime expiresLdt = expiresTs.toLocalDateTime();
            st.setExpiresAt(expiresLdt);
        }
        
        return st;
    }
}