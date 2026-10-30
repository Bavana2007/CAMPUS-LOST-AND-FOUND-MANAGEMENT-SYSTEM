package model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model representing a lost or found item.
 */
public class Item {
    private int itemId;
    private String title;
    private String description;
    private String category;
    private String location;
    private Date dateReported;
    private String status; // 'lost', 'found', 'claimed'
    private int reportedBy; // user_id of reporter
    private Timestamp createdAt;
    
    // Auxiliary display field from join
    private String reporterName;

    public Item() {
    }

    public Item(int itemId, String title, String description, String category,
                String location, Date dateReported, String status, int reportedBy) {
        this.itemId = itemId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.location = location;
        this.dateReported = dateReported;
        this.status = status;
        this.reportedBy = reportedBy;
    }

    public Item(String title, String description, String category,
                String location, Date dateReported, String status, int reportedBy) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.location = location;
        this.dateReported = dateReported;
        this.status = status;
        this.reportedBy = reportedBy;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getDateReported() {
        return dateReported;
    }

    public void setDateReported(Date dateReported) {
        this.dateReported = dateReported;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(int reportedBy) {
        this.reportedBy = reportedBy;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    @Override
    public String toString() {
        return "#" + itemId + " - " + title + " [" + status + "]";
    }
}
