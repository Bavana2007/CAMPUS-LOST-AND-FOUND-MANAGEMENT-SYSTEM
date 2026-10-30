package ui;

import dao.ItemDAO;
import model.Item;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;

/**
 * Form for Adding and Editing Lost and Found items.
 */
public class ItemForm extends JDialog {

    private final User currentUser;
    private final ItemDAO itemDAO = new ItemDAO();
    private final Runnable onSuccessCallback;

    private Item itemToEdit = null; // null if adding new item

    private JTextField titleField;
    private JComboBox<String> categoryCombo;
    private JTextField locationField;
    private JTextField dateField;
    private JComboBox<String> statusCombo;
    private JTextArea descArea;

    private static final String[] CATEGORIES = {
        "Electronics", "Accessories", "Books & Stationery", "Cards & Documents",
        "Clothing & Apparel", "Keys", "Personal Belongings", "Other"
    };

    /**
     * Constructor for creating a NEW item.
     */
    public ItemForm(Frame owner, User user, Runnable onSuccess) {
        super(owner, "Report New Lost or Found Item", true);
        this.currentUser = user;
        this.onSuccessCallback = onSuccess;
        initDialog();
    }

    /**
     * Constructor for EDITING an existing item.
     */
    public ItemForm(Frame owner, User user, Item itemToEdit, Runnable onSuccess) {
        super(owner, "Edit Item Details - #" + itemToEdit.getItemId(), true);
        this.currentUser = user;
        this.itemToEdit = itemToEdit;
        this.onSuccessCallback = onSuccess;
        initDialog();
        populateFields();
    }

    private void initDialog() {
        setSize(480, 520);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel headerTitle = new JLabel(itemToEdit == null ? "REPORT AN ITEM" : "EDIT ITEM DETAILS", SwingConstants.LEFT);
        headerTitle.setFont(UITheme.HEADER_FONT);
        headerTitle.setForeground(Color.WHITE);
        headerPanel.add(headerTitle, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Form Fields
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BG);
        formPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblTitle = new JLabel("Title / Item Name:*");
        lblTitle.setFont(UITheme.BOLD_FONT);
        formPanel.add(lblTitle, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        titleField = new JTextField(20);
        formPanel.add(titleField, gbc);

        // 2. Category
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblCat = new JLabel("Category:*");
        lblCat.setFont(UITheme.BOLD_FONT);
        formPanel.add(lblCat, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        categoryCombo = new JComboBox<>(CATEGORIES);
        categoryCombo.setEditable(true);
        formPanel.add(categoryCombo, gbc);

        // 3. Location
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblLoc = new JLabel("Location:*");
        lblLoc.setFont(UITheme.BOLD_FONT);
        formPanel.add(lblLoc, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        locationField = new JTextField(20);
        formPanel.add(locationField, gbc);

        // 4. Date Reported (YYYY-MM-DD)
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblDate = new JLabel("Date (YYYY-MM-DD):*");
        lblDate.setFont(UITheme.BOLD_FONT);
        formPanel.add(lblDate, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        dateField = new JTextField(LocalDate.now().toString(), 20);
        formPanel.add(dateField, gbc);

        // 5. Status
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel lblStatus = new JLabel("Status:*");
        lblStatus.setFont(UITheme.BOLD_FONT);
        formPanel.add(lblStatus, gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        statusCombo = new JComboBox<>(new String[]{"lost", "found", "claimed"});
        formPanel.add(statusCombo, gbc);

        // 6. Description
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel lblDesc = new JLabel("Description:");
        lblDesc.setFont(UITheme.BOLD_FONT);
        formPanel.add(lblDesc, gbc);

        gbc.gridx = 1; gbc.gridy = 5;
        descArea = new JTextArea(4, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(descArea);
        formPanel.add(scrollDesc, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Bottom Actions
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(UITheme.BG_LIGHT);

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        btnPanel.add(cancelBtn);

        JButton saveBtn = UITheme.createPrimaryButton(itemToEdit == null ? "Save Item" : "Update Item");
        saveBtn.addActionListener(e -> handleSave());
        btnPanel.add(saveBtn);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void populateFields() {
        if (itemToEdit == null) return;
        titleField.setText(itemToEdit.getTitle());
        categoryCombo.setSelectedItem(itemToEdit.getCategory());
        locationField.setText(itemToEdit.getLocation());
        dateField.setText(itemToEdit.getDateReported().toString());
        statusCombo.setSelectedItem(itemToEdit.getStatus());
        descArea.setText(itemToEdit.getDescription());
    }

    private void handleSave() {
        String title = titleField.getText().trim();
        String category = (String) categoryCombo.getSelectedItem();
        String location = locationField.getText().trim();
        String dateStr = dateField.getText().trim();
        String status = (String) statusCombo.getSelectedItem();
        String desc = descArea.getText().trim();

        // Validations
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title/Item name cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            titleField.requestFocus();
            return;
        }

        if (category == null || category.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Category is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (location.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Location cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            locationField.requestFocus();
            return;
        }

        if (dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Date is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            dateField.requestFocus();
            return;
        }

        Date sqlDate;
        try {
            sqlDate = Date.valueOf(dateStr);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD (e.g. 2026-10-30).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            dateField.requestFocus();
            return;
        }

        try {
            if (itemToEdit == null) {
                // Insert New
                Item newItem = new Item(title, desc, category, location, sqlDate, status, currentUser.getUserId());
                boolean success = itemDAO.addItem(newItem);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Item reported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    if (onSuccessCallback != null) onSuccessCallback.run();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to save item.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Update Existing
                itemToEdit.setTitle(title);
                itemToEdit.setDescription(desc);
                itemToEdit.setCategory(category);
                itemToEdit.setLocation(location);
                itemToEdit.setDateReported(sqlDate);
                itemToEdit.setStatus(status);

                boolean success = itemDAO.updateItem(itemToEdit);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Item updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    if (onSuccessCallback != null) onSuccessCallback.run();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update item.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
