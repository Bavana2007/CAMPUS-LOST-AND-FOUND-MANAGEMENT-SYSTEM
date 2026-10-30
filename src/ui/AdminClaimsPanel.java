package ui;

import dao.ClaimDAO;
import model.Claim;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Admin Panel for reviewing, approving, and rejecting claims.
 * Approving a claim sets item status to 'claimed' and automatically rejects other pending claims
 * via JDBC transaction with commit/rollback.
 */
public class AdminClaimsPanel extends JPanel {

    private final ClaimDAO claimDAO = new ClaimDAO();
    private final Frame parentFrame;

    private JTable claimsTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterCombo;
    private JLabel statusCountLabel;
    private List<Claim> currentClaimsList;

    public AdminClaimsPanel(Frame parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(12, 16, 12, 16));

        initComponents();
        loadClaims();
    }

    private void initComponents() {
        // Top Toolbar
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        // Header and counter
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("CLAIMS MANAGEMENT DASHBOARD");
        title.setFont(UITheme.HEADER_FONT);
        title.setForeground(UITheme.TEXT_DARK);

        statusCountLabel = new JLabel("Loading claims data...");
        statusCountLabel.setFont(UITheme.SMALL_FONT);
        statusCountLabel.setForeground(UITheme.TEXT_MUTED);

        titlePanel.add(title);
        titlePanel.add(statusCountLabel);
        topPanel.add(titlePanel, BorderLayout.WEST);

        // Filter Controls
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterPanel.setOpaque(false);

        filterPanel.add(new JLabel("Filter:"));
        filterCombo = new JComboBox<>(new String[]{"Pending Claims Only", "All Claims History"});
        filterCombo.addActionListener(e -> loadClaims());
        filterPanel.add(filterCombo);

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadClaims());
        filterPanel.add(refreshBtn);

        topPanel.add(filterPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Claims Table
        String[] columns = {
            "Claim ID", "Item ID", "Item Title", "Category", "Claimant Name", "Claimant Email",
            "Message / Proof", "Claim Date", "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only
            }
        };

        claimsTable = new JTable(tableModel);
        claimsTable.setRowHeight(28);
        claimsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        claimsTable.getTableHeader().setReorderingAllowed(false);

        // Column widths
        claimsTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        claimsTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        claimsTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        claimsTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        claimsTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        claimsTable.getColumnModel().getColumn(5).setPreferredWidth(160);
        claimsTable.getColumnModel().getColumn(6).setPreferredWidth(220);
        claimsTable.getColumnModel().getColumn(7).setPreferredWidth(130);
        claimsTable.getColumnModel().getColumn(8).setPreferredWidth(90);

        // Color renderer for status
        claimsTable.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value != null ? value.toString() : "";
                if (!isSelected) {
                    if ("pending".equalsIgnoreCase(status)) {
                        c.setForeground(UITheme.WARNING_COLOR);
                        setFont(UITheme.BOLD_FONT);
                    } else if ("approved".equalsIgnoreCase(status)) {
                        c.setForeground(UITheme.SUCCESS_COLOR);
                        setFont(UITheme.BOLD_FONT);
                    } else if ("rejected".equalsIgnoreCase(status)) {
                        c.setForeground(UITheme.DANGER_COLOR);
                        setFont(UITheme.BOLD_FONT);
                    }
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(claimsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Action Toolbar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(8, 0, 0, 0));

        JLabel hint = new JLabel("Select a pending claim to Approve or Reject.");
        hint.setFont(UITheme.SMALL_FONT);
        hint.setForeground(UITheme.TEXT_MUTED);
        bottomBar.add(hint, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtns.setOpaque(false);

        JButton viewDetailsBtn = UITheme.createSecondaryButton("View Full Message");
        viewDetailsBtn.addActionListener(e -> showMessageDialog());
        actionBtns.add(viewDetailsBtn);

        JButton rejectBtn = UITheme.createColoredButton("Reject Claim", UITheme.DANGER_COLOR);
        rejectBtn.addActionListener(e -> handleRejectClaim());
        actionBtns.add(rejectBtn);

        JButton approveBtn = UITheme.createColoredButton("Approve Claim", UITheme.SUCCESS_COLOR);
        approveBtn.addActionListener(e -> handleApproveClaim());
        actionBtns.add(approveBtn);

        bottomBar.add(actionBtns, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);
    }

    public void loadClaims() {
        tableModel.setRowCount(0);
        boolean pendingOnly = filterCombo.getSelectedIndex() == 0;

        try {
            if (pendingOnly) {
                currentClaimsList = claimDAO.getPendingClaims();
            } else {
                currentClaimsList = claimDAO.getAllClaims();
            }

            for (Claim c : currentClaimsList) {
                tableModel.addRow(new Object[]{
                    c.getClaimId(),
                    c.getItemId(),
                    c.getItemTitle(),
                    c.getItemCategory(),
                    c.getClaimantName(),
                    c.getClaimantEmail(),
                    c.getMessage(),
                    c.getClaimDate() != null ? c.getClaimDate().toString().substring(0, 16) : "-",
                    c.getStatus().toUpperCase()
                });
            }

            int pendingCount = 0;
            if (pendingOnly) {
                pendingCount = currentClaimsList.size();
            } else {
                for (Claim c : currentClaimsList) {
                    if ("pending".equalsIgnoreCase(c.getStatus())) pendingCount++;
                }
            }
            statusCountLabel.setText("Total Displayed: " + currentClaimsList.size() + " | Pending Action: " + pendingCount);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load claims: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Claim getSelectedClaim() {
        int selectedRow = claimsTable.getSelectedRow();
        if (selectedRow < 0 || currentClaimsList == null || selectedRow >= currentClaimsList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a claim from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return currentClaimsList.get(selectedRow);
    }

    private void showMessageDialog() {
        Claim claim = getSelectedClaim();
        if (claim == null) return;

        JTextArea area = new JTextArea(claim.getMessage(), 10, 35);
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        area.setEditable(false);
        area.setFont(UITheme.BODY_FONT);

        JOptionPane.showMessageDialog(
            this,
            new JScrollPane(area),
            "Claim Message: Item #" + claim.getItemId() + " - " + claim.getItemTitle(),
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void handleApproveClaim() {
        Claim claim = getSelectedClaim();
        if (claim == null) return;

        if (!"pending".equalsIgnoreCase(claim.getStatus())) {
            JOptionPane.showMessageDialog(this,
                "This claim has already been resolved with status: " + claim.getStatus().toUpperCase(),
                "Action Not Allowed",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to APPROVE Claim #" + claim.getClaimId() + " for '" + claim.getItemTitle() + "'?\n\n"
            + "Notice: Approving this claim will:\n"
            + "1. Set Claim #" + claim.getClaimId() + " to APPROVED.\n"
            + "2. Mark Item #" + claim.getItemId() + " status as 'CLAIMED'.\n"
            + "3. Automatically REJECT any other pending claims on this item.\n\n"
            + "This action will execute atomically within a database transaction.",
            "Confirm Claim Approval",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean success = claimDAO.approveClaimTransaction(claim.getClaimId(), claim.getItemId());
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Claim successfully approved!\nThe item has been marked as claimed, and conflicting pending claims were rejected.",
                    "Claim Approved",
                    JOptionPane.INFORMATION_MESSAGE);
                loadClaims();
            } else {
                JOptionPane.showMessageDialog(this, "Approval failed. The claim or item was not updated.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Transaction Error: " + ex.getMessage(), "Transaction Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRejectClaim() {
        Claim claim = getSelectedClaim();
        if (claim == null) return;

        if (!"pending".equalsIgnoreCase(claim.getStatus())) {
            JOptionPane.showMessageDialog(this,
                "This claim has already been resolved with status: " + claim.getStatus().toUpperCase(),
                "Action Not Allowed",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to REJECT Claim #" + claim.getClaimId() + "?",
            "Confirm Rejection",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean success = claimDAO.rejectClaim(claim.getClaimId());
            if (success) {
                JOptionPane.showMessageDialog(this, "Claim marked as rejected.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadClaims();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reject claim.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
