import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Small smoke test for the Member 1 JDBC deliverable.
 * Run after MySQL Server, database.sql, and Connector/J are configured.
 */
public class DBConnectionTest {
    public static void main(String[] args) {
        try (Connection connection = DBConnection.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();

            System.out.println("JDBC connection successful.");
            System.out.println("Database: " + metadata.getDatabaseProductName());
            System.out.println("Version: " + metadata.getDatabaseProductVersion());
            System.out.println("URL: " + metadata.getURL());

            String[] expectedTables = {"users", "items", "claims"};
            for (String table : expectedTables) {
                try (ResultSet rs = connection.getMetaData()
                        .getTables(connection.getCatalog(), null, table,
                                new String[]{"TABLE"})) {
                    System.out.println(table + " table exists: " + rs.next());
                }
            }
        } catch (SQLException e) {
            System.err.println("JDBC connection failed.");
            System.err.println("Check that MySQL is running, database exists,");
            System.err.println("Connector/J is on the classpath, and credentials are correct.");
            System.err.println("Reason: " + e.getMessage());
            System.exit(1);
        }
    }
}
