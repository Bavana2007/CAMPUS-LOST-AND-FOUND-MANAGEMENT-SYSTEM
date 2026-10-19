package campuslostandfound;

import javax.swing.*;
import java.awt.*;

public class CampusLostAndFound {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame(
                "Campus Lost & Found Management System"
            );

            JLabel title = new JLabel(
                "Campus Lost & Found Management System",
                SwingConstants.CENTER
            );

            JButton addButton = new JButton("Add Item");
            JButton viewButton = new JButton("View Items");
            JButton searchButton = new JButton("Search Items");
            JButton claimButton = new JButton("Claim Item");

            addButton.addActionListener(e -> {
                new ItemForm().setVisible(true);
            });

            viewButton.addActionListener(e -> {
                new ViewItems().setVisible(true);
            });

            claimButton.addActionListener(e -> {
                new ClaimForm().setVisible(true);
            });

            // Search functionality will be connected later.
            searchButton.addActionListener(e -> {
                JOptionPane.showMessageDialog(
                    frame,
                    "Search functionality is not connected yet."
                );
            });

            JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
            panel.setBorder(
                BorderFactory.createEmptyBorder(25, 40, 25, 40)
            );

            panel.add(title);
            panel.add(addButton);
            panel.add(viewButton);
            panel.add(searchButton);
            panel.add(claimButton);

            frame.add(panel);
            frame.setSize(600, 350);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
