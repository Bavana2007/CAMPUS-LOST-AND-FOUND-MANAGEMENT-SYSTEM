package dao;

import db.DBConnection;
import model.Item;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Item operations (CRUD, searching, and filtering).
 */
public class ItemDAO {

    /**
     * Adds a new lost or found item.
     * @param item Item to insert
     * @return true if inserted, false otherwise
     */
    public boolean addItem(Item item) throws SQLException {
        String sql = "INSERT INTO items (title, description, category, location, date_reported, status, reported_by) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, item.getTitle().trim());
            ps.setString(2, item.getDescription() != null ? item.getDescription().trim() : "");
            ps.setString(3, item.getCategory().trim());
            ps.setString(4, item.getLocation().trim());
            ps.setDate(5, item.getDateReported());
            ps.setString(6, item.getStatus().toLowerCase().trim());
            ps.setInt(7, item.getReportedBy());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setItemId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Updates an existing item.
     * @param item Item with updated values
     * @return true if updated, false otherwise
     */
    public boolean updateItem(Item item) throws SQLException {
        String sql = "UPDATE items SET title = ?, description = ?, category = ?, "
                   + "location = ?, date_reported = ?, status = ? WHERE item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, item.getTitle().trim());
            ps.setString(2, item.getDescription() != null ? item.getDescription().trim() : "");
            ps.setString(3, item.getCategory().trim());
            ps.setString(4, item.getLocation().trim());
            ps.setDate(5, item.getDateReported());
            ps.setString(6, item.getStatus().toLowerCase().trim());
            ps.setInt(7, item.getItemId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes an item by its ID.
     */
    public boolean deleteItem(int itemId) throws SQLException {
        String sql = "DELETE FROM items WHERE item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves an item by its primary key.
     */
    public Item getItemById(int itemId) throws SQLException {
        String sql = "SELECT i.item_id, i.title, i.description, i.category, i.location, "
                   + "i.date_reported, i.status, i.reported_by, i.created_at, u.name AS reporter_name "
                   + "FROM items i "
                   + "LEFT JOIN users u ON i.reported_by = u.user_id "
                   + "WHERE i.item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all items from newest to oldest.
     */
    public List<Item> getAllItems() throws SQLException {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT i.item_id, i.title, i.description, i.category, i.location, "
                   + "i.date_reported, i.status, i.reported_by, i.created_at, u.name AS reporter_name "
                   + "FROM items i "
                   + "LEFT JOIN users u ON i.reported_by = u.user_id "
                   + "ORDER BY i.date_reported DESC, i.item_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(mapRow(rs));
            }
        }
        return items;
    }

    /**
     * Search and filter items based on keyword (title, category, location) and status.
     * @param keyword search phrase (or empty/null for all)
     * @param statusFilter status filter ("lost", "found", "claimed", or "ALL"/empty for all)
     */
    public List<Item> searchAndFilterItems(String keyword, String statusFilter) throws SQLException {
        List<Item> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT i.item_id, i.title, i.description, i.category, i.location, "
            + "i.date_reported, i.status, i.reported_by, i.created_at, u.name AS reporter_name "
            + "FROM items i "
            + "LEFT JOIN users u ON i.reported_by = u.user_id "
            + "WHERE 1=1 "
        );

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasStatus = statusFilter != null && !statusFilter.trim().isEmpty() && !statusFilter.equalsIgnoreCase("ALL");

        if (hasKeyword) {
            sql.append("AND (i.title LIKE ? OR i.category LIKE ? OR i.location LIKE ? OR i.description LIKE ?) ");
        }
        if (hasStatus) {
            sql.append("AND i.status = ? ");
        }

        sql.append("ORDER BY i.date_reported DESC, i.item_id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            if (hasKeyword) {
                String pattern = "%" + keyword.trim() + "%";
                ps.setString(paramIndex++, pattern);
                ps.setString(paramIndex++, pattern);
                ps.setString(paramIndex++, pattern);
                ps.setString(paramIndex++, pattern);
            }
            if (hasStatus) {
                ps.setString(paramIndex, statusFilter.trim().toLowerCase());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves items reported by a specific user.
     */
    public List<Item> getItemsByReporter(int userId) throws SQLException {
        List<Item> list = new ArrayList<>();
        String sql = "SELECT i.item_id, i.title, i.description, i.category, i.location, "
                   + "i.date_reported, i.status, i.reported_by, i.created_at, u.name AS reporter_name "
                   + "FROM items i "
                   + "LEFT JOIN users u ON i.reported_by = u.user_id "
                   + "WHERE i.reported_by = ? "
                   + "ORDER BY i.date_reported DESC, i.item_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Updates an item's status.
     */
    public boolean updateStatus(int itemId, String newStatus) throws SQLException {
        String sql = "UPDATE items SET status = ? WHERE item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.trim().toLowerCase());
            ps.setInt(2, itemId);
            return ps.executeUpdate() > 0;
        }
    }

    private Item mapRow(ResultSet rs) throws SQLException {
        Item item = new Item(
            rs.getInt("item_id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("category"),
            rs.getString("location"),
            rs.getDate("date_reported"),
            rs.getString("status"),
            rs.getInt("reported_by")
        );
        item.setCreatedAt(rs.getTimestamp("created_at"));
        item.setReporterName(rs.getString("reporter_name"));
        return item;
    }
}
