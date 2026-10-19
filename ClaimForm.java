package campuslostandfound;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClaimForm extends JFrame {

    private JTextField itemIdField;
    private JTextField userIdField;
    private JTextArea descriptionArea;

    public ClaimForm() {

        setTitle("Claim Found Item");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );

        panel.add(new JLabel("Item ID:"));
        itemIdField = new JTextField();
        panel.add(itemIdField);

        panel.add(new JLabel("Your User ID:"));
        userIdField = new JTextField();
        panel.add(userIdField);

        panel.add(new JLabel("Claim Description:"));
        descriptionArea = new JTextArea(3, 15);
        panel.add(new JScrollPane(descriptionArea));

        JButton clearButton = new JButton("Clear");
        JButton submitButton = new JButton("Submit Claim");

        panel.add(clearButton);
        panel.add(submitButton);

        clearButton.addActionListener(e -> {
            itemIdField.setText("");
            userIdField.setText("");
            descriptionArea.setText("");
        });

        submitButton.addActionListener(e -> submitClaim());

        add(panel);
    }

    private void submitClaim() {

        String itemText = itemIdField.getText().trim();
        String userText = userIdField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (itemText.isEmpty() || userText.isEmpty()
                || description.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Please fill in all fields."
            );
            return;
        }

        int itemId;
        int userId;

        try {
            itemId = Integer.parseInt(itemText);
            userId = Integer.parseInt(userText);

            if (itemId <= 0 || userId <= 0) {
                JOptionPane.showMessageDialog(
                    this, "Enter valid positive IDs."
                );
                return;
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this, "Item ID and User ID must be numbers."
            );
            return;
        }

        String sql = "INSERT INTO claims "
            + "(item_id, claimant_id, claim_description, "
            + "claim_date, status) "
            + "VALUES (?, ?, ?, CURRENT_DATE, 'PENDING')";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            ps.setInt(2, userId);
            ps.setString(3, description);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                this, "Claim submitted successfully! Status: PENDING"
            );

            itemIdField.setText("");
            userIdField.setText("");
            descriptionArea.setText("");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Unable to submit claim: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClaimForm().setVisible(true);
        });
    }
}