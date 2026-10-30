package db;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton database connection manager.
 * Reads the URL, user, and password from config/db.properties,
 * environment variables, or fallback defaults.
 */
public final class DBConnection {

    private static String url = "jdbc:mysql://localhost:3306/campus_lost_found?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String user = "root";
    private static String password = "";

    static {
        // 1. Try to load from config/db.properties
        try {
            Properties props = new Properties();
            if (Files.exists(Paths.get("config/db.properties"))) {
                try (InputStream in = Files.newInputStream(Paths.get("config/db.properties"))) {
                    props.load(in);
                }
            } else if (Files.exists(Paths.get("db.properties"))) {
                try (InputStream in = Files.newInputStream(Paths.get("db.properties"))) {
                    props.load(in);
                }
            }

            if (props.getProperty("db.url") != null) {
                url = props.getProperty("db.url").trim();
            }
            if (props.getProperty("db.user") != null) {
                user = props.getProperty("db.user").trim();
            }
            if (props.getProperty("db.password") != null) {
                password = props.getProperty("db.password").trim();
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load config/db.properties, using defaults/env.");
        }

        // 2. Override with environment variables if present
        String envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            url = envUrl;
        }

        String envUser = System.getenv("DB_USER");
        if (envUser != null && !envUser.isBlank()) {
            user = envUser;
        }

        String envPass = System.getenv("DB_PASSWORD");
        if (envPass == null) {
            envPass = System.getenv("CLF_DB_PASSWORD");
        }
        if (envPass != null) {
            password = envPass;
        }

        // Explicitly register MySQL driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
            // Driver will be auto-loaded via SPI if on classpath
        }
    }

    private DBConnection() {
        // Prevent direct instantiation (singleton pattern)
    }

    /**
     * Programmatic credential setter (useful for runtime login or dynamic configuration).
     */
    public static synchronized void configure(String newUrl, String newUser, String newPassword) {
        if (newUrl != null) url = newUrl;
        if (newUser != null) user = newUser;
        if (newPassword != null) password = newPassword;
    }

    public static String getUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }

    public static String getPassword() {
        return password;
    }

    /**
     * Returns an active database connection.
     * @return Connection instance
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
