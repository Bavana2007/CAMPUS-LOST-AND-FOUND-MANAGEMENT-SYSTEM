package dao;

import db.DBConnection;
import model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User operations (Registration, Authentication, Profile lookup).
 */
public class UserDAO {

    /**
     * Compute SHA-256 hex digest for secure password storage.
     */
    public static String hashPassword(String password) {
        if (password == null) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 hashing error", e);
        }
    }

    /**
     * Verifies password against stored hash or plaintext demo password.
     */
    public static boolean checkPassword(String plainPassword, String storedHashOrPassword) {
        if (plainPassword == null || storedHashOrPassword == null) {
            return false;
        }
        // Direct match (for initial database.sql sample rows)
        if (plainPassword.equals(storedHashOrPassword)) {
            return true;
        }
        // SHA-256 hash match
        String hashed = hashPassword(plainPassword);
        return hashed.equalsIgnoreCase(storedHashOrPassword);
    }

    /**
     * Authenticates a user by email and password.
     * @return User object on success, or null on failure.
     */
    public User login(String email, String password) throws SQLException {
        String sql = "SELECT user_id, name, email, password, role, created_at FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (checkPassword(password, storedPassword)) {
                        return new User(
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            storedPassword,
                            rs.getString("role"),
                            rs.getTimestamp("created_at")
                        );
                    }
                }
            }
        }
        return null;
    }

    /**
     * Checks whether an email address is already registered.
     */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Registers a new user. The password is automatically hashed with SHA-256.
     * @return true if successfully inserted, false otherwise.
     */
    public boolean register(User user) throws SQLException {
        if (emailExists(user.getEmail())) {
            return false;
        }

        String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, user.getName().trim());
            ps.setString(2, user.getEmail().trim().toLowerCase());
            // Store hashed password
            ps.setString(3, hashPassword(user.getPassword()));
            String role = (user.getRole() != null && user.getRole().equalsIgnoreCase("admin")) ? "admin" : "student";
            ps.setString(4, role);

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setUserId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves user by their primary key.
     */
    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT user_id, name, email, password, role, created_at FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getTimestamp("created_at")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all registered users.
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, name, email, password, role, created_at FROM users ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(new User(
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getTimestamp("created_at")
                ));
            }
        }
        return list;
    }
}
