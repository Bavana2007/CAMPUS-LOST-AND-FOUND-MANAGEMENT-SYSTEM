package ui;

import dao.ClaimDAO;
import model.Claim;
import model.Item;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/**
 * Claim submission dialog.
 * Students select an item, write a message with proof of ownership,
 * and submit a claim. Blocks duplicate claims and claims on already claimed items.
 */
public class ClaimForm extends JDialog {

    private final User currentUser;
    private final Item item;
    private final ClaimDAO claimDAO = new ClaimDAO();
    private final Runnable onSuccessCallback;

    private JTextArea messageArea;

    public ClaimForm(Frame owner, User currentUser, Item item, Runnable onSuccess) {
        super(owner, "Submit Claim - Item #" + item.getItemId(), true);
        this.currentUser = currentUser;
        this.item = item;
        this.onSuccessCallback = onSuccess;

        initDialog();
    }

    private void initDialog() {
        setSize(520, 500);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel titleLbl = new JLabel("SUBMIT CLAIM OF OWNERSHIP");
        titleLbl.setFont(UITheme.HEADER_FONT);
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel("Item #" + item.getItemId() + " - " + item.getTitle());
        subLbl.setFont(UITheme.BODY_FONT);
        subLbl.setForeground(new Color(220, 230, 245));

        headerPanel.add(titleLbl, BorderLayout.NORTH);
        headerPanel.add(subLbl, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(UITheme.CARD_BG);
        contentPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Item Summary Card
        JPanel itemCard = new JPanel(new GridLayout(4, 2, 6, 6));
        itemCard.setBorder(BorderFactory.createTitledBorder("Item Details"));
        itemCard.setBackground(UITheme.BG_LIGHT);

        itemCard.add(new JLabel("Title:"));
        JLabel valTitle = new JLabel("<html><b>" + item.getTitle() + "</b></html>");
        itemCard.add(valTitle);

        itemCard.add(new JLabel("Category:"));
        itemCard.add(new JLabel(item.getCategory()));

        itemCard.add(new JLabel("Location Found/Lost:"));
        itemCard.add(new JLabel(item.getLocation()));

        itemCard.add(new JLabel("Current Status:"));
        JLabel valStatus = new JLabel(item.getStatus().toUpperCase());
        if ("claimed".equalsIgnoreCase(item.getStatus())) {
            valStatus.setForeground(UITheme.DANGER_COLOR);
        } else {
            valStatus.setForeground(UITheme.SUCCESS_COLOR);
        }
        itemCard.add(valStatus);

        contentPanel.add(itemCard, BorderLayout.NORTH);

        // Claim Message area
        JPanel msgPanel = new JPanel(new BorderLayout(6, 6));
        msgPanel.setBackground(UITheme.CARD_BG);
        JLabel msgPrompt = new JLabel("<html><b>Proof of Ownership / Message:</b><br><font color='#7f8c8d'>Describe identifying characteristics, exact contents, serial numbers, or when you lost it.</font></html>");
        msgPanel.add(msgPrompt, BorderLayout.NORTH);

        messageArea = new JTextArea(5, 20);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(UITheme.BODY_FONT);
        JScrollPane scroll = new JScrollPane(messageArea);
        msgPanel.add(scroll, BorderLayout.CENTER);

        contentPanel.add(msgPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        // Bottom Actions
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(UITheme.BG_LIGHT);

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        btnPanel.add(cancelBtn);

        JButton submitBtn = UITheme.createColoredButton("Submit Claim", UITheme.SUCCESS_COLOR);
        submitBtn.addActionListener(e -> handleSubmitClaim());
        btnPanel.add(submitBtn);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void handleSubmitClaim() {
        String msg = messageArea.getText().trim();

        // 1. Validation: Empty or too short message
        if (msg.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a message describing your proof of ownership.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            messageArea.requestFocus();
            return;
        }

        if (msg.length() < 5) {
            JOptionPane.showMessageDialog(this,
                "Please provide a more detailed description of ownership (at least 5 characters).",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            messageArea.requestFocus();
            return;
        }

        // 2. Business Rule: Check if item already claimed
        if ("claimed".equalsIgnoreCase(item.getStatus())) {
            JOptionPane.showMessageDialog(this,
                "This item has already been claimed and closed. No new claims can be submitted.",
                "Item Already Claimed",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // 3. Business Rule: Check duplicate claims by same user on same item
            if (claimDAO.hasUserClaimedItem(item.getItemId(), currentUser.getUserId())) {
                JOptionPane.showMessageDialog(this,
                    "Duplicate Claim: You have already submitted a claim for this item.\nPlease wait for administrator review.",
                    "Claim Already Submitted",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Submit claim
            Claim claim = new Claim(item.getItemId(), currentUser.getUserId(), msg);
            boolean created = claimDAO.submitClaim(claim);

            if (created) {
                JOptionPane.showMessageDialog(this,
                    "Claim submitted successfully!\nThe administrator will review your claim and reach out.",
                    "Claim Submitted",
                    JOptionPane.INFORMATION_MESSAGE);
                if (onSuccessCallback != null) onSuccessCallback.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to submit claim. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }

        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Claim Blocked", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
