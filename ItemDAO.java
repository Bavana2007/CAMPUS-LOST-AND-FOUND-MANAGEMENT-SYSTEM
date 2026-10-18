
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemDAO {

    
public boolean validateItem(String name, String description,
                            String location, String status) {
    return name != null && !name.isBlank()
            && location != null && !location.isBlank()
            && status != null && !status.isBlank();
}

  
public void addItem(int userId, String name, String category,
                    String description, String location,
                    String itemDate, String reportType) {
                        if (!validateItem(name, description, location, reportType)) {
    System.out.println("Invalid item details.");
    return;
}

    String sql = "INSERT INTO items "
            + "(item_name, description, category, location, "
            + "date_reported, status, reported_by) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, name);
        ps.setString(2, description);
        ps.setString(3, category);
        ps.setString(4, location);
        ps.setDate(5, java.sql.Date.valueOf(itemDate));
        ps.setString(6, reportType);
        ps.setInt(7, userId);

        int rows = ps.executeUpdate();

        if (rows > 0) {
            System.out.println("Item added successfully!");
        }

    } catch (SQLException | IllegalArgumentException e) {
        System.out.println("Error adding item: " + e.getMessage());
    }
}


    public void viewItems() {
        String sql = "SELECT * FROM items";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean found = false;

            while (rs.next()) {
                found = true;

                System.out.println("Item ID: " + rs.getInt("item_id"));
                System.out.println("Item Name: " + rs.getString("item_name"));
                System.out.println("Category: " + rs.getString("category"));
                System.out.println("Description: " + rs.getString("description"));
                System.out.println("Location: " + rs.getString("location"));
                System.out.println("----------------------");
            }

            if (!found) {
                System.out.println("No items found.");
            }

        } catch (SQLException e) {
            System.out.println("Error viewing items: " + e.getMessage());
        }
    }

    public void updateItem(int itemId, String name,
                       String category, String description,
                       String location, String status) {

    String sql = "UPDATE items SET item_name = ?, "
            + "category = ?, description = ?, "
            + "location = ?, status = ? "
            + "WHERE item_id = ?";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, name);
        ps.setString(2, category);
        ps.setString(3, description);
        ps.setString(4, location);
        ps.setString(5, status);
        ps.setInt(6, itemId);

        int rows = ps.executeUpdate();

        if (rows > 0) {
            System.out.println("Item updated successfully!");
        } else {
            System.out.println("Item ID not found.");
        }

    } catch (SQLException e) {
        System.out.println("Error updating item: " + e.getMessage());
    }
}

    
public void deleteItem(int itemId) {
    String sql = "DELETE FROM items WHERE item_id = ?";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, itemId);

        int rows = ps.executeUpdate();

        if (rows > 0) {
            System.out.println("Item deleted successfully!");
        } else {
            System.out.println("Item ID not found.");
        }

    } catch (SQLException e) {
        System.out.println("Error deleting item: " + e.getMessage());
    }
}


    
public void searchItems(String keyword) {
    String sql = "SELECT * FROM items "
            + "WHERE item_name LIKE ? "
            + "OR category LIKE ?";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, "%" + keyword + "%");
        ps.setString(2, "%" + keyword + "%");

        try (ResultSet rs = ps.executeQuery()) {
            boolean found = false;

            while (rs.next()) {
                found = true;
                System.out.println("Item ID: " + rs.getInt("item_id"));
                System.out.println("Item Name: " + rs.getString("item_name"));
                System.out.println("Category: " + rs.getString("category"));
                System.out.println("Location: " + rs.getString("location"));
                System.out.println("----------------------");
            }

            if (!found) {
                System.out.println("No matching items found.");
            }
        }

    } catch (SQLException e) {
        System.out.println("Error searching items: " + e.getMessage());
    }
}

public void filterByStatus(String status) {
    String sql = "SELECT * FROM items WHERE status = ?";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, status);

        try (ResultSet rs = ps.executeQuery()) {
            boolean found = false;

            while (rs.next()) {
                found = true;

                System.out.println("Item ID: " + rs.getInt("item_id"));
                System.out.println("Item Name: " + rs.getString("item_name"));
                System.out.println("Category: " + rs.getString("category"));
                System.out.println("Location: " + rs.getString("location"));
                System.out.println("Status: " + rs.getString("status"));
                System.out.println("----------------------");
            }

            if (!found) {
                System.out.println("No items found with status: " + status);
            }
        }

    } catch (SQLException e) {
        System.out.println("Error filtering items: " + e.getMessage());
    }
}
}



