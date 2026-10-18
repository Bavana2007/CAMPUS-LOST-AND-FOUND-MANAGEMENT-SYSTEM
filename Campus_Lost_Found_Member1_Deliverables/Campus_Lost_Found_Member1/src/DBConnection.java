import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Shared JDBC connection utility for Campus Lost & Found.
 *
 * Requirements:
 * - JDK 17 or later recommended
 * - MySQL Server running locally
 * - MySQL Connector/J added to the project classpath
 *
 * Configure credentials using environment variables where possible:
 *   CLF_DB_URL      (optional; defaults to local campus_lost_found database)
 *   CLF_DB_USER     (optional; defaults to root)
 *   CLF_DB_PASSWORD (recommended; set to your local MySQL password)
 *
 * Do not commit real passwords to a shared repository.
 */
public final class DBConnection {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/campus_lost_found"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private DBConnection() {
        // Utility class; do not instantiate.
    }

    public static Connection getConnection() throws SQLException {
        String url = envOrDefault("CLF_DB_URL", DEFAULT_URL);
        String user = envOrDefault("CLF_DB_USER", "root");
        String password = System.getenv("CLF_DB_PASSWORD");

        if (password == null) {
            password = "";
        }

        return DriverManager.getConnection(url, user, password);
    }

    private static String envOrDefault(String name, String fallback) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
