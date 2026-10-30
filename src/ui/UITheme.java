package ui;

import db.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Styling and theme helper for modern Nimbus Look and Feel with clean typography.
 */
public final class UITheme {

    public static final Color PRIMARY_COLOR = new Color(28, 78, 158);       // Campus Royal Blue
    public static final Color SECONDARY_COLOR = new Color(41, 128, 185);    // Sky accent
    public static final Color SUCCESS_COLOR = new Color(39, 174, 96);       // Emerald Green
    public static final Color DANGER_COLOR = new Color(192, 57, 43);        // Crimson Red
    public static final Color WARNING_COLOR = new Color(211, 84, 0);        // Amber Orange
    public static final Color BG_LIGHT = new Color(245, 247, 250);          // Soft grey-white
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_DARK = new Color(44, 62, 80);
    public static final Color TEXT_MUTED = new Color(127, 140, 141);

    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 11);

    private UITheme() {
    }

    /**
     * Applies Nimbus look & feel across the desktop application.
     */
    public static void applyLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // Fallback to system default if Nimbus is unavailable
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // ignore
            }
        }

        UIManager.put("defaultFont", BODY_FONT);
        UIManager.put("Label.font", BODY_FONT);
        UIManager.put("Button.font", BOLD_FONT);
        UIManager.put("TextField.font", BODY_FONT);
        UIManager.put("Table.font", BODY_FONT);
        UIManager.put("TableHeader.font", HEADER_FONT);
    }

    /**
     * Creates a styled primary action button.
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(BOLD_FONT);
        btn.setBackground(PRIMARY_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR.darker(), 1),
            new EmptyBorder(8, 16, 8, 16)
        ));
        return btn;
    }

    /**
     * Creates a styled secondary/outline button.
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(BOLD_FONT);
        btn.setBackground(new Color(236, 240, 241));
        btn.setForeground(TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(8, 16, 8, 16)
        ));
        return btn;
    }

    /**
     * Creates an alert action button (e.g. Danger/Approve).
     */
    public static JButton createColoredButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(BOLD_FONT);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 1),
            new EmptyBorder(8, 16, 8, 16)
        ));
        return btn;
    }

    /**
     * Show modern DB settings dialog.
     */
    public static void showDatabaseConfigDialog(Component parent) {
        JTextField urlField = new JTextField(DBConnection.getUrl(), 26);
        JTextField userField = new JTextField(DBConnection.getUser(), 26);
        JPasswordField passField = new JPasswordField(DBConnection.getPassword(), 26);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.add(new JLabel("JDBC URL:"));
        panel.add(urlField);
        panel.add(new JLabel("MySQL User:"));
        panel.add(userField);
        panel.add(new JLabel("MySQL Password:"));
        panel.add(passField);

        int result = JOptionPane.showConfirmDialog(
            parent,
            panel,
            "Configure Database Credentials",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            DBConnection.configure(
                urlField.getText().trim(),
                userField.getText().trim(),
                new String(passField.getPassword())
            );
            JOptionPane.showMessageDialog(
                parent,
                "Database credentials updated for this session.",
                "Updated",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
}
