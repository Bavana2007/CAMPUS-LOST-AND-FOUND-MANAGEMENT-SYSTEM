
package campuslostandfound;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DBConnectionTest {

    private DBConnectionTest() {
    }

    public static void main(String[] args) {
        String sql =
            "SELECT item_id, title, item_status "
            + "FROM items ORDER BY item_id";

        try (
            Connection connection = DBConnection.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)
        ) {
            System.out.println(
                "PASS: JDBC connection and items query succeeded."
            );

            while (resultSet.next()) {
                System.out.printf(
                    "%d | %s | %s%n",
                    resultSet.getInt("item_id"),
                    resultSet.getString("title"),
                    resultSet.getString("item_status")
                );
            }
        } catch (SQLException e) {
            System.err.println("FAIL: JDBC test failed.");
            System.err.println("Details: " + e.getMessage());
            System.exit(1);
        }
    }
}
