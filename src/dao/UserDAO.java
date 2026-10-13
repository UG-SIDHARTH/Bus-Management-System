package dao;

import database.DBConnection;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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

    public static final List<User> SEEDED_FALLBACK_USERS = List.of(
        new User(1, "admin", "admin@swiftbus.local", "c49d61715c488c16c308ffa2aac8bf78265a6a418ad7b79dce96ffa62b4a8b28", "2acfec13c3b1548fc74ca21496de8f7e", "ADMIN", null),
        new User(2, "john_doe", "john@example.com", "3b1f5c66b8d234a667ec38a4c1647ea36014ba361309f44cf64b85da8158d601", "f0e1d2c3b4a5968778695a4b3c2d1e0f", "USER", null),
        new User(3, "test1", "test1@swiftbus.local", "c49d61715c488c16c308ffa2aac8bf78265a6a418ad7b79dce96ffa62b4a8b28", "2acfec13c3b1548fc74ca21496de8f7e", "USER", null),
        new User(4, "test2", "test2@swiftbus.local", "90ea0553f4c66a7f097db59e216a4a8ab7af0cc60441abcdf5630087828434b0", "2af96a45d3d19bbf76e9c7d52728f9e0", "USER", null),
        new User(5, "test3", "test3@swiftbus.local", "b13f1f69b9bc542ba6d9c68e8c0a455456d4790763c1f8629f3d2791c23e69e8", "06ae62b3adba6e9cac924438aa501325", "USER", null),
        new User(6, "test4", "test4@swiftbus.local", "7a2de55354226f2c3c8a714bcdde8b25dbbf7bfd79f37be2f7653d4f340e0eed", "a8abd5e482785b1a979e97c987f5430d", "USER", null),
        new User(7, "test5", "test5@swiftbus.local", "2af439a4c4b5565de8a196f00f99fbc16759c43a195b0ddf6ad1cbae156d9658", "956dccb74876f321b9bdfaad4bc17701", "USER", null)
    );

    private User getFallbackUser(String identifier) {
        if (identifier == null) return null;
        for (User u : SEEDED_FALLBACK_USERS) {
            if (u.getUsername().equalsIgnoreCase(identifier) || u.getEmail().equalsIgnoreCase(identifier)) {
                return new User(u.getId(), u.getUsername(), u.getEmail(), u.getPasswordHash(), u.getSalt(), u.getRole(), u.getCreatedAt());
            }
        }
        return null;
    }

    private User getFallbackUserById(int id) {
        for (User u : SEEDED_FALLBACK_USERS) {
            if (u.getId() == id) {
                return new User(u.getId(), u.getUsername(), u.getEmail(), u.getPasswordHash(), u.getSalt(), u.getRole(), u.getCreatedAt());
            }
        }
        return null;
    }

    public User findByUsernameOrEmail(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }
        String cleanId = identifier.trim().toLowerCase();
        String query = "SELECT id, username, email, password_hash, salt, role, created_at FROM users " +
                       "WHERE LOWER(username) = ? OR LOWER(email) = ? LIMIT 1";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return getFallbackUser(cleanId);
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
        return getFallbackUser(cleanId);
    }

    public User findById(int id) {
        String query = "SELECT id, username, email, password_hash, salt, role, created_at FROM users WHERE id = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return getFallbackUserById(id);
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
        return getFallbackUserById(id);
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String query = "SELECT id, username, email, password_hash, salt, role, created_at FROM users ORDER BY id ASC";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return new ArrayList<>(SEEDED_FALLBACK_USERS);
            try (PreparedStatement pst = con.prepareStatement(query);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getString("salt"),
                            rs.getString("role"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] getAllUsers error: " + e.getMessage());
        }
        return list.isEmpty() ? new ArrayList<>(SEEDED_FALLBACK_USERS) : list;
    }

    public List<User> getAllUsersWithStats() {
        List<User> list = new ArrayList<>();
        String query = "SELECT u.id, u.username, u.email, u.password_hash, u.salt, u.role, u.created_at, " +
                       "COUNT(b.booking_id) AS booking_count, " +
                       "COALESCE(SUM(CASE WHEN b.status = 'CONFIRMED' THEN b.fare ELSE 0 END), 0) AS total_spent " +
                       "FROM users u " +
                       "LEFT JOIN booking b ON u.id = b.user_id " +
                       "GROUP BY u.id, u.username, u.email, u.password_hash, u.salt, u.role, u.created_at " +
                       "ORDER BY u.id ASC";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return getAllUsers();
            try (PreparedStatement pst = con.prepareStatement(query);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    User u = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getString("salt"),
                            rs.getString("role"),
                            rs.getTimestamp("created_at")
                    );
                    u.setBookingCount(rs.getInt("booking_count"));
                    u.setTotalSpent(rs.getDouble("total_spent"));
                    list.add(u);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] getAllUsersWithStats error, falling back: " + e.getMessage());
            return getAllUsers();
        }
        return list.isEmpty() ? getAllUsers() : list;
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
