package app.dao;

import app.config.ConnectionDB;
import app.models.SecurityToken;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SecurityTokenDAO {

    // 1. Insertar: Registra el trámite (Cambio de correo, clave, etc.)
    public boolean insertToken(SecurityToken st) {
        String sql = "INSERT INTO SecurityTokens (UserUuid, TokenType, TokenCode, NewValue, ExpiresAt) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, st.getUserUuid());
            ps.setString(2, st.getTokenType());
            ps.setString(3, st.getTokenCode());
            ps.setString(4, st.getNewValue());
            ps.setTimestamp(5, st.getExpiresAt());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return false; 
        }
    }

    // 2. Validar: Busca un token activo y no expirado
    public SecurityToken validateToken(String uuid, String code, String type) {
        String sql = "SELECT * FROM SecurityTokens WHERE UserUuid = ? AND TokenCode = ? AND TokenType = ? " +
                     "AND IsUsed = FALSE AND ExpiresAt > CURRENT_TIMESTAMP";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid);
            ps.setString(2, code);
            ps.setString(3, type);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapSecurityToken(rs);
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return null;
    }

    // 3. ¡EL MÉTODO QUE FALTABA!: Marcar como usado (Quemar el token)
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

    // 4. Historial: Para ver los movimientos de un usuario
    public List<SecurityToken> getTokensByUser(String uuid) {
        List<SecurityToken> list = new ArrayList<>();
        String sql = "SELECT * FROM SecurityTokens WHERE UserUuid = ? ORDER BY CreatedAt DESC LIMIT 50";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapSecurityToken(rs)); 
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return list;
    }

    // 5. Mapeador: Centraliza la creación del objeto desde la DB
    private SecurityToken mapSecurityToken(ResultSet rs) throws SQLException {
        SecurityToken st = new SecurityToken();
        st.setIdToken(rs.getInt("IdToken"));
        st.setUserUuid(rs.getString("UserUuid"));
        st.setTokenType(rs.getString("TokenType"));
        st.setTokenCode(rs.getString("TokenCode"));
        st.setNewValue(rs.getString("NewValue"));
        st.setUsed(rs.getBoolean("IsUsed"));
        
        Timestamp createdTs = rs.getTimestamp("CreatedAt");
        if (createdTs != null) st.setCreatedAt(createdTs);
        
        Timestamp expiresTs = rs.getTimestamp("ExpiresAt");
        if (expiresTs != null) st.setExpiresAt(expiresTs);
        
        return st;
    }
}