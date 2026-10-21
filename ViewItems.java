package campuslostandfound;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class ViewItems extends JFrame {

    public ViewItems() {

        setTitle("View Lost and Found Items");
        setSize(750, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columns = {
            "Item ID", "Item Name", "Category",
            "Type", "Location", "Date", "Status"
        };

        javax.swing.table.DefaultTableModel model =
            new javax.swing.table.DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

        JTable table = new JTable(model);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        loadItems(model);
    }

    private void loadItems(
            javax.swing.table.DefaultTableModel model) {

        String sql = "SELECT item_id, item_name, category, report_type, "
           + "location, item_date, status "
           + "FROM items ORDER BY item_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd-MM-yyyy");

            while (rs.next()) {

    int itemId = rs.getInt("item_id");
    String itemName = rs.getString("item_name");
    String category = rs.getString("category");
    String type = rs.getString("report_type");
    String location = rs.getString("location");

    String date = rs.getDate("item_date")
                    .toLocalDate()
                    .format(formatter);

    String status = rs.getString("status");

    model.addRow(new Object[]{
        itemId,
        itemName,
        category,
        type,
        location,
        date,
        status
    });
}

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                this,
                "Unable to load items: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ViewItems().setVisible(true);
        });
    }
}
