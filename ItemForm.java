package campuslostandfound;
import javax.swing.*;
import java.awt.*;
public class ItemForm extends JFrame{
     JTextField nameField, locationField, dateField;
    JTextArea descriptionArea;
    JComboBox<String> categoryBox, typeBox;

    public ItemForm() {

        setTitle("Add Lost or Found Item");
        setSize(450, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

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

        submitButton.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                    this, "Please enter the item name."
                );
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Form completed. Database saving will be added next."
                );
            }
        });

        add(panel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ItemForm().setVisible(true);
        });
    }
}