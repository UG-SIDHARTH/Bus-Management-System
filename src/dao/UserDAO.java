package dao;

import database.DBConnection;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAO {

    public boolean createUser(User user) {
        String query = "INSERT INTO users (username, email, password_hash, salt, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return false;
            try (PreparedStatement pst = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pst.setString(1, user.getUsername());
                pst.setString(2, user.getEmail());
                pst.setString(3, user.getPasswordHash());
                pst.setString(4, user.getSalt());
                pst.setString(5, user.getRole() != null ? user.getRole() : "USER");

                int rows = pst.executeUpdate();
                if (rows > 0) {
                    try (ResultSet keys = pst.getGeneratedKeys()) {
                        if (keys.next()) {
                            user.setId(keys.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] createUser error: " + e.getMessage());
        }
        return false;
    }

    public User findByUsernameOrEmail(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }
        String cleanId = identifier.trim().toLowerCase();
        String query = "SELECT id, username, email, password_hash, salt, role, created_at FROM users " +
                       "WHERE LOWER(username) = ? OR LOWER(email) = ? LIMIT 1";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return null;
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setString(1, cleanId);
                pst.setString(2, cleanId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                rs.getString("email"),
                                rs.getString("password_hash"),
                                rs.getString("salt"),
                                rs.getString("role"),
                                rs.getTimestamp("created_at")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] findByUsernameOrEmail error: " + e.getMessage());
        }
        return null;
    }

    public User findById(int id) {
        String query = "SELECT id, username, email, password_hash, salt, role, created_at FROM users WHERE id = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return null;
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, id);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                rs.getString("email"),
                                rs.getString("password_hash"),
                                rs.getString("salt"),
                                rs.getString("role"),
                                rs.getTimestamp("created_at")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] findById error: " + e.getMessage());
        }
        return null;
    }

    public boolean isUsernameTaken(String username) {
        if (username == null) return false;
        String query = "SELECT COUNT(*) FROM users WHERE LOWER(username) = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return false;
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setString(1, username.trim().toLowerCase());
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] isUsernameTaken error: " + e.getMessage());
        }
        return false;
    }

    public boolean isEmailTaken(String email) {
        if (email == null) return false;
        String query = "SELECT COUNT(*) FROM users WHERE LOWER(email) = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return false;
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setString(1, email.trim().toLowerCase());
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] isEmailTaken error: " + e.getMessage());
        }
        return false;
    }
}
