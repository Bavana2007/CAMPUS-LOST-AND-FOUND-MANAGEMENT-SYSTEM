package model;

import java.sql.Timestamp;

/**
 * Model representing a claim submitted by a student for an item.
 */
public class Claim {
    private int claimId;
    private int itemId;
    private int claimantId;
    private String message;
    private String status; // 'pending', 'approved', 'rejected'
    private Timestamp claimDate;

    // Joined auxiliary fields for UI display
    private String itemTitle;
    private String claimantName;
    private String claimantEmail;
    private String itemCategory;
    private String itemLocation;

    public Claim() {
    }

    public Claim(int claimId, int itemId, int claimantId, String message, String status, Timestamp claimDate) {
        this.claimId = claimId;
        this.itemId = itemId;
        this.claimantId = claimantId;
        this.message = message;
        this.status = status;
        this.claimDate = claimDate;
    }

    public Claim(int itemId, int claimantId, String message) {
        this.itemId = itemId;
        this.claimantId = claimantId;
        this.message = message;
        this.status = "pending";
    }

    public int getClaimId() {
        return claimId;
    }

    public void setClaimId(int claimId) {
        this.claimId = claimId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getClaimantId() {
        return claimantId;
    }

    public void setClaimantId(int claimantId) {
        this.claimantId = claimantId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(Timestamp claimDate) {
        this.claimDate = claimDate;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public String getClaimantName() {
        return claimantName;
    }

    public void setClaimantName(String claimantName) {
        this.claimantName = claimantName;
    }

    public String getClaimantEmail() {
        return claimantEmail;
    }

    public void setClaimantEmail(String claimantEmail) {
        this.claimantEmail = claimantEmail;
    }

    public String getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
    }

    public String getItemLocation() {
        return itemLocation;
    }

    public void setItemLocation(String itemLocation) {
        this.itemLocation = itemLocation;
    }

    @Override
    public String toString() {
        return "Claim #" + claimId + " for Item #" + itemId + " (" + status + ")";
    }
}
