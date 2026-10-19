package campuslostandfound;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ItemForm extends JFrame {

    JTextField nameField, locationField, dateField;
    JTextArea descriptionArea;
    JComboBox<String> categoryBox, typeBox;
    
    private Runnable onSaved;

    public ItemForm() {
        
        this(null);
    }
    public ItemForm(Runnable onSaved){
        this.onSaved=onSaved;
        setTitle("Add Lost or Found Item");
        setSize(450, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.setBorder(
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );

        panel.add(new JLabel("Item Name:"));
        nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel("Description:"));
        descriptionArea = new JTextArea(3, 15);
        panel.add(new JScrollPane(descriptionArea));

        panel.add(new JLabel("Category:"));
        categoryBox = new JComboBox<>(new String[]{
            "ID Card", "Books", "Keys",
            "Water Bottle", "Electronics", "Other"
        });
        panel.add(categoryBox);

        panel.add(new JLabel("Type:"));
        typeBox = new JComboBox<>(new String[]{
            "Lost", "Found"
        });
        panel.add(typeBox);

        panel.add(new JLabel("Location:"));
        locationField = new JTextField();
        panel.add(locationField);

        panel.add(new JLabel("Date (DD-MM-YYYY):"));
        dateField = new JTextField();
        panel.add(dateField);

        JButton clearButton = new JButton("Clear");
        JButton submitButton = new JButton("Submit");

        panel.add(clearButton);
        panel.add(submitButton);

        clearButton.addActionListener(e -> {
            nameField.setText("");
            descriptionArea.setText("");
            locationField.setText("");
            dateField.setText("");
            categoryBox.setSelectedIndex(0);
            typeBox.setSelectedIndex(0);
        });

        submitButton.addActionListener(e -> saveItem());

        add(panel);
    }

    private void saveItem() {

        String name = nameField.getText().trim();
        String description = descriptionArea.getText().trim();
        String category = (String) categoryBox.getSelectedItem();
        String type = ((String) typeBox.getSelectedItem()).toUpperCase();
        String location = locationField.getText().trim();
        String dateText = dateField.getText().trim();

        if (name.isEmpty() || location.isEmpty()
                || dateText.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Please enter the item name, location and date."
            );
            return;
        }

        LocalDate itemDate;

        try {
            DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd-MM-uuuu");

            itemDate = LocalDate.parse(dateText, formatter);

        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(
                this, "Enter the date as DD-MM-YYYY, for example 19-10-2026."
            );
            return;
        }

        String sql = "INSERT INTO items "
            + "(user_id, item_name, category, description, "
            + "location, item_date, report_type) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        // Change 1 to the ID of an existing user in your users table.
        int userId = 1;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.setString(3, category);
            ps.setString(4, description);
            ps.setString(5, location);
            ps.setDate(6, java.sql.Date.valueOf(itemDate));
            ps.setString(7, type);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                this, "Item saved successfully!"
            );
            
            if(onSaved != null){
                onSaved.run();
            }
            nameField.setText("");
            descriptionArea.setText("");
            locationField.setText("");
            dateField.setText("");
            categoryBox.setSelectedIndex(0);
            typeBox.setSelectedIndex(0);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Unable to save item: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ItemForm().setVisible(true);
        });
        
    }
}
