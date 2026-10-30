package ui;

import dao.ItemDAO;
import model.Item;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Main item browsing, searching, filtering, and CRUD management view.
 */
public class ViewItems extends JPanel {

    private final ItemDAO itemDAO = new ItemDAO();
    private final Frame parentFrame;
    private final User currentUser;

    private JTable itemsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private JLabel countLabel;

    private List<Item> displayedItems;

    public ViewItems(Frame parent, User user) {
        this.parentFrame = parent;
        this.currentUser = user;

        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(12, 16, 12, 16));

        initComponents();
        loadItems();
    }

    private void initComponents() {
        // TOP FILTER & SEARCH BAR
        JPanel topContainer = new JPanel(new BorderLayout(10, 8));
        topContainer.setOpaque(false);

        // Header Title & Count
        JPanel headerInfo = new JPanel(new GridLayout(2, 1));
        headerInfo.setOpaque(false);
        JLabel pageTitle = new JLabel("BROWSE LOST & FOUND ITEMS");
        pageTitle.setFont(UITheme.HEADER_FONT);
        pageTitle.setForeground(UITheme.TEXT_DARK);

        countLabel = new JLabel("Loading items...");
        countLabel.setFont(UITheme.SMALL_FONT);
        countLabel.setForeground(UITheme.TEXT_MUTED);

        headerInfo.add(pageTitle);
        headerInfo.add(countLabel);
        topContainer.add(headerInfo, BorderLayout.WEST);

        // Search & Filter Controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        controls.setOpaque(false);

        controls.add(new JLabel("Search:"));
        searchField = new JTextField(16);
        searchField.setToolTipText("Search title, category, location, or description");
        // Real-time search update
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadItems(); }
            public void removeUpdate(DocumentEvent e) { loadItems(); }
            public void changedUpdate(DocumentEvent e) { loadItems(); }
        });
        controls.add(searchField);

        controls.add(new JLabel("Status:"));
        statusFilterCombo = new JComboBox<>(new String[]{"ALL", "lost", "found", "claimed"});
        statusFilterCombo.addActionListener(e -> loadItems());
        controls.add(statusFilterCombo);

        JButton clearBtn = UITheme.createSecondaryButton("Clear Filters");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilterCombo.setSelectedIndex(0);
            loadItems();
        });
        controls.add(clearBtn);

        topContainer.add(controls, BorderLayout.EAST);
        add(topContainer, BorderLayout.NORTH);

        // ITEMS TABLE
        String[] columns = {
            "ID", "Title", "Category", "Location", "Date Reported", "Status", "Reported By"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        itemsTable = new JTable(tableModel);
        itemsTable.setRowHeight(28);
        itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemsTable.getTableHeader().setReorderingAllowed(false);

        // Adjust column widths
        itemsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        itemsTable.getColumnModel().getColumn(1).setPreferredWidth(210);
        itemsTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        itemsTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        itemsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        itemsTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        itemsTable.getColumnModel().getColumn(6).setPreferredWidth(130);

        // Status Column Custom Renderer
        itemsTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String val = value != null ? value.toString() : "";
                if (!isSelected) {
                    if ("lost".equalsIgnoreCase(val)) {
                        c.setForeground(UITheme.WARNING_COLOR);
                        setFont(UITheme.BOLD_FONT);
                    } else if ("found".equalsIgnoreCase(val)) {
                        c.setForeground(UITheme.SECONDARY_COLOR);
                        setFont(UITheme.BOLD_FONT);
                    } else if ("claimed".equalsIgnoreCase(val)) {
                        c.setForeground(UITheme.SUCCESS_COLOR);
                        setFont(UITheme.BOLD_FONT);
                    }
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        JScrollPane tableScroll = new JScrollPane(itemsTable);
        add(tableScroll, BorderLayout.CENTER);

        // BOTTOM ACTION TOOLBAR
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(8, 0, 0, 0));

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.setOpaque(false);

        JButton viewDetailsBtn = UITheme.createSecondaryButton("View Item Details");
        viewDetailsBtn.addActionListener(e -> showItemDetails());
        leftActions.add(viewDetailsBtn);

        JButton reportNewBtn = UITheme.createPrimaryButton("+ Report New Item");
        reportNewBtn.addActionListener(e -> openAddItemDialog());
        leftActions.add(reportNewBtn);

        bottomBar.add(leftActions, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        // Claim button (Available to students)
        if (currentUser.isStudent()) {
            JButton claimBtn = UITheme.createColoredButton("Claim This Item", UITheme.SUCCESS_COLOR);
            claimBtn.addActionListener(e -> openClaimDialog());
            rightActions.add(claimBtn);
        }

        // Edit button
        JButton editBtn = UITheme.createSecondaryButton("Edit Item");
        editBtn.addActionListener(e -> openEditItemDialog());
        rightActions.add(editBtn);

        // Delete button
        JButton deleteBtn = UITheme.createColoredButton("Delete Item", UITheme.DANGER_COLOR);
        deleteBtn.addActionListener(e -> handleDeleteItem());
        rightActions.add(deleteBtn);

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadItems());
        rightActions.add(refreshBtn);

        bottomBar.add(rightActions, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);
    }

    public void loadItems() {
        tableModel.setRowCount(0);
        String keyword = searchField != null ? searchField.getText().trim() : "";
        String status = statusFilterCombo != null ? (String) statusFilterCombo.getSelectedItem() : "ALL";

        try {
            displayedItems = itemDAO.searchAndFilterItems(keyword, status);
            for (Item item : displayedItems) {
                tableModel.addRow(new Object[]{
                    item.getItemId(),
                    item.getTitle(),
                    item.getCategory(),
                    item.getLocation(),
                    item.getDateReported() != null ? item.getDateReported().toString() : "-",
                    item.getStatus().toUpperCase(),
                    item.getReporterName() != null ? item.getReporterName() : "User #" + item.getReportedBy()
                });
            }
            if (countLabel != null) {
                countLabel.setText("Found " + displayedItems.size() + " items matching criteria.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load items: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Item getSelectedItem() {
        int row = itemsTable.getSelectedRow();
        if (row < 0 || displayedItems == null || row >= displayedItems.size()) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return displayedItems.get(row);
    }

    private void showItemDetails() {
        Item item = getSelectedItem();
        if (item == null) return;

        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 6));
        panel.add(new JLabel("Item ID:"));
        panel.add(new JLabel("#" + item.getItemId()));
        panel.add(new JLabel("Title:"));
        panel.add(new JLabel(item.getTitle()));
        panel.add(new JLabel("Category:"));
        panel.add(new JLabel(item.getCategory()));
        panel.add(new JLabel("Location:"));
        panel.add(new JLabel(item.getLocation()));
        panel.add(new JLabel("Reported Date:"));
        panel.add(new JLabel(item.getDateReported().toString()));
        panel.add(new JLabel("Current Status:"));
        panel.add(new JLabel(item.getStatus().toUpperCase()));

        JTextArea descArea = new JTextArea(item.getDescription() != null ? item.getDescription() : "No description provided.", 5, 25);
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);

        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.add(panel, BorderLayout.NORTH);
        wrapper.add(new JLabel("Description:"), BorderLayout.CENTER);
        wrapper.add(new JScrollPane(descArea), BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, wrapper, "Item Details - #" + item.getItemId(), JOptionPane.PLAIN_MESSAGE);
    }

    private void openAddItemDialog() {
        ItemForm form = new ItemForm(parentFrame, currentUser, this::loadItems);
        form.setVisible(true);
    }

    private void openEditItemDialog() {
        Item item = getSelectedItem();
        if (item == null) return;

        // Permission check: admin can edit any; students can only edit items they reported
        if (!currentUser.isAdmin() && item.getReportedBy() != currentUser.getUserId()) {
            JOptionPane.showMessageDialog(this,
                "Permission Denied: You can only edit items that you reported.",
                "Access Denied",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        ItemForm form = new ItemForm(parentFrame, currentUser, item, this::loadItems);
        form.setVisible(true);
    }

    private void handleDeleteItem() {
        Item item = getSelectedItem();
        if (item == null) return;

        // Permission check: admin can delete any; students can only delete items they reported
        if (!currentUser.isAdmin() && item.getReportedBy() != currentUser.getUserId()) {
            JOptionPane.showMessageDialog(this,
                "Permission Denied: You can only delete items that you reported.",
                "Access Denied",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete item #" + item.getItemId() + " ('" + item.getTitle() + "')?\n"
            + "Any related claims will also be deleted.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean success = itemDAO.deleteItem(item.getItemId());
            if (success) {
                JOptionPane.showMessageDialog(this, "Item deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                loadItems();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete item.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openClaimDialog() {
        Item item = getSelectedItem();
        if (item == null) return;

        if ("claimed".equalsIgnoreCase(item.getStatus())) {
            JOptionPane.showMessageDialog(this,
                "This item has already been claimed and marked as resolved.",
                "Item Claimed",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (item.getReportedBy() == currentUser.getUserId()) {
            JOptionPane.showMessageDialog(this,
                "You are the reporter of this item. You cannot submit a claim on your own report.",
                "Notice",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ClaimForm claimForm = new ClaimForm(parentFrame, currentUser, item, this::loadItems);
        claimForm.setVisible(true);
    }
}
