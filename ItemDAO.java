package campuslostandfound;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemDAO {
public boolean validateItem(String name, String description,
                            String location, String status) {
    return name != null && !name.isBlank()
            && location != null && !location.isBlank()
            && status != null
            && (status.equalsIgnoreCase("OPEN")
                || status.equalsIgnoreCase("CLAIMED")
                || status.equalsIgnoreCase("RETURNED"));
}
public void addItem(int userId, String name, String category,
                    String description, String location,
                    String itemDate, String reportType) {

    if (name == null || name.isBlank()
            || category == null || category.isBlank()
            || location == null || location.isBlank()
            || itemDate == null || itemDate.isBlank()
            || reportType == null
            || !(reportType.equalsIgnoreCase("LOST")
                 || reportType.equalsIgnoreCase("FOUND"))) {
        System.out.println("Invalid item details.");
        return;
    }

    String sql = "INSERT INTO items "
            + "(user_id, item_name, category, description, "
            + "location, item_date, report_type, status) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, 'OPEN')";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, userId);
        ps.setString(2, name);
        ps.setString(3, category);
        ps.setString(4, description);
        ps.setString(5, location);
        ps.setDate(6, java.sql.Date.valueOf(itemDate));
        ps.setString(7, reportType.toUpperCase());

        int rows = ps.executeUpdate();

        if (rows > 0) {
            System.out.println("Item added successfully.");
        }

    } catch (Exception e) {
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

