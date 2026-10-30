package dao;

import db.DBConnection;
import model.Claim;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Claim operations.
 * Handles claim submission, duplicate prevention, and transactional approvals.
 */
public class ClaimDAO {

    /**
     * Checks if a user has already submitted a claim on the specified item.
     */
    public boolean hasUserClaimedItem(int itemId, int claimantId) throws SQLException {
        String sql = "SELECT 1 FROM claims WHERE item_id = ? AND claimant_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setInt(2, claimantId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Checks if the item is already marked as 'claimed'.
     */
    public boolean isItemClaimed(int itemId) throws SQLException {
        String sql = "SELECT status FROM items WHERE item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "claimed".equalsIgnoreCase(rs.getString("status"));
                }
            }
        }
        return false;
    }

    /**
     * Submits a claim for an item.
     * Enforces rules:
     * - Item cannot already be 'claimed'.
     * - Duplicate claims by the same claimant on the same item are blocked.
     * @return true if claim was recorded, false if validation/business rule failed
     */
    public boolean submitClaim(Claim claim) throws SQLException, IllegalStateException {
        // Business Rule 1: Block claims on items already marked 'claimed'
        if (isItemClaimed(claim.getItemId())) {
            throw new IllegalStateException("This item is already marked as claimed.");
        }

        // Business Rule 2: Block duplicate claims on the same item by the same user
        if (hasUserClaimedItem(claim.getItemId(), claim.getClaimantId())) {
            throw new IllegalStateException("You have already submitted a claim for this item.");
        }

        String sql = "INSERT INTO claims (item_id, claimant_id, message, status) VALUES (?, ?, ?, 'pending')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, claim.getItemId());
            ps.setInt(2, claim.getClaimantId());
            ps.setString(3, claim.getMessage().trim());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        claim.setClaimId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Transactional Claim Approval:
     * 1. Approves the target claim (status -> 'approved').
     * 2. Sets the item status to 'claimed'.
     * 3. Automatically rejects any other pending claims on that item.
     * Uses JDBC transaction commit & rollback.
     */
    public boolean approveClaimTransaction(int claimId, int itemId) throws SQLException {
        String approveSql = "UPDATE claims SET status = 'approved' WHERE claim_id = ?";
        String itemClaimedSql = "UPDATE items SET status = 'claimed' WHERE item_id = ?";
        String rejectOthersSql = "UPDATE claims SET status = 'rejected' WHERE item_id = ? AND claim_id != ? AND status = 'pending'";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // 1. Approve selected claim
            try (PreparedStatement ps1 = conn.prepareStatement(approveSql)) {
                ps1.setInt(1, claimId);
                int r1 = ps1.executeUpdate();
                if (r1 == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 2. Mark item as claimed
            try (PreparedStatement ps2 = conn.prepareStatement(itemClaimedSql)) {
                ps2.setInt(1, itemId);
                int r2 = ps2.executeUpdate();
                if (r2 == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 3. Reject all other pending claims on the same item
            try (PreparedStatement ps3 = conn.prepareStatement(rejectOthersSql)) {
                ps3.setInt(1, itemId);
                ps3.setInt(2, claimId);
                ps3.executeUpdate();
            }

            conn.commit(); // Commit transaction successfully
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback changes upon error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Rejects a single claim.
     */
    public boolean rejectClaim(int claimId) throws SQLException {
        String sql = "UPDATE claims SET status = 'rejected' WHERE claim_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, claimId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all claims with joined item and claimant details.
     */
    public List<Claim> getAllClaims() throws SQLException {
        List<Claim> list = new ArrayList<>();
        String sql = "SELECT c.claim_id, c.item_id, c.claimant_id, c.message, c.status, c.claim_date, "
                   + "i.title AS item_title, i.category AS item_category, i.location AS item_location, "
                   + "u.name AS claimant_name, u.email AS claimant_email "
                   + "FROM claims c "
                   + "JOIN items i ON c.item_id = i.item_id "
                   + "JOIN users u ON c.claimant_id = u.user_id "
                   + "ORDER BY c.claim_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapClaim(rs));
            }
        }
        return list;
    }

    /**
     * Retrieves pending claims for admin review.
     */
    public List<Claim> getPendingClaims() throws SQLException {
        List<Claim> list = new ArrayList<>();
        String sql = "SELECT c.claim_id, c.item_id, c.claimant_id, c.message, c.status, c.claim_date, "
                   + "i.title AS item_title, i.category AS item_category, i.location AS item_location, "
                   + "u.name AS claimant_name, u.email AS claimant_email "
                   + "FROM claims c "
                   + "JOIN items i ON c.item_id = i.item_id "
                   + "JOIN users u ON c.claimant_id = u.user_id "
                   + "WHERE c.status = 'pending' "
                   + "ORDER BY c.claim_date ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapClaim(rs));
            }
        }
        return list;
    }

    /**
     * Retrieves claims submitted by a specific student.
     */
    public List<Claim> getClaimsByClaimant(int claimantId) throws SQLException {
        List<Claim> list = new ArrayList<>();
        String sql = "SELECT c.claim_id, c.item_id, c.claimant_id, c.message, c.status, c.claim_date, "
                   + "i.title AS item_title, i.category AS item_category, i.location AS item_location, "
                   + "u.name AS claimant_name, u.email AS claimant_email "
                   + "FROM claims c "
                   + "JOIN items i ON c.item_id = i.item_id "
                   + "JOIN users u ON c.claimant_id = u.user_id "
                   + "WHERE c.claimant_id = ? "
                   + "ORDER BY c.claim_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, claimantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapClaim(rs));
                }
            }
        }
        return list;
    }

    private Claim mapClaim(ResultSet rs) throws SQLException {
        Claim claim = new Claim(
            rs.getInt("claim_id"),
            rs.getInt("item_id"),
            rs.getInt("claimant_id"),
            rs.getString("message"),
            rs.getString("status"),
            rs.getTimestamp("claim_date")
        );
        claim.setItemTitle(rs.getString("item_title"));
        claim.setItemCategory(rs.getString("item_category"));
        claim.setItemLocation(rs.getString("item_location"));
        claim.setClaimantName(rs.getString("claimant_name"));
        claim.setClaimantEmail(rs.getString("claimant_email"));
        return claim;
    }
}
