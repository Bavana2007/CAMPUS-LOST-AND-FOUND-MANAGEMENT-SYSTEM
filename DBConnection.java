
package campuslostandfound;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {

    private static final String URL =
        "jdbc:mysql://localhost:3306/lost_found_db?useSSL=false&serverTimezone=UTC";

    private static final String DB_USER = "root";

    private DBConnection() {
        // Prevent object creation.
    }

    public static Connection getConnection() throws SQLException {
        String password = System.getenv("CLF_DB_PASSWORD");

        if (password == null) {
            password = "";
        }

        return DriverManager.getConnection(
            URL, DB_USER, password
        );
    }

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            System.out.println(
                "SUCCESS: Connected to lost_found_db."
            );
            System.out.println(
                "Database: " + connection.getCatalog()
            );
        } catch (SQLException e) {
            System.err.println(
                "ERROR: MySQL connection failed."
            );
            System.err.println(
                "Check MySQL, database name, username, "
                + "CLF_DB_PASSWORD and JDBC driver."
            );
            System.err.println("Details: " + e.getMessage());
            System.exit(1);
        }
    }
}
