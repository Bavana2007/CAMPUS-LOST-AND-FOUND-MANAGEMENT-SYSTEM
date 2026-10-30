
package campuslostandfound;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SearchItems extends JFrame {

    private JTextField searchField;
    private JTable table;
    private DefaultTableModel model;

    public SearchItems() {

        setTitle("Search Lost and Found Items");
        setSize(850, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Enter item name, category or location:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);

        String[] columns = {
            "Item ID", "Item Name", "Category",
            "Type", "Location", "Date", "Status"
        };

        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        searchButton.addActionListener(e -> searchItems());

        searchField.addActionListener(e -> searchItems());
    }

    private void searchItems() {

        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Please enter a search keyword."
            );
            return;
        }

        String sql = "SELECT item_id, item_name, category, "
                + "report_type, location, item_date, status "
                + "FROM items "
                + "WHERE item_name LIKE ? OR category LIKE ? "
                + "OR location LIKE ? OR report_type LIKE ? "
                + "OR status LIKE ? "
                + "ORDER BY item_id DESC";

        model.setRowCount(0);

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String searchValue = "%" + keyword + "%";

            for (int i = 1; i <= 5; i++) {
                ps.setString(i, searchValue);
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    model.addRow(new Object[]{
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getString("report_type"),
                        rs.getString("location"),
                        rs.getDate("item_date"),
                        rs.getString("status")
                    });
                }
            }

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(
                    this, "No matching items found."
                );
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Unable to search items: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
