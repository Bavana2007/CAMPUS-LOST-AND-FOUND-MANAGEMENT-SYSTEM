package campuslostandfound;
import javax.swing.*;
import java.awt.*;
public class ViewItems extends JFrame{
    public ViewItems() {

        setTitle("View Lost and Found Items");
        setSize(750, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columns = {
            "Item Name", "Category", "Type",
            "Location", "Date", "Status"
        };

        Object[][] data = {
            {"Water Bottle", "Water Bottle", "Lost",
                "Library", "15-10-2026", "Missing"},
            {"ID Card", "ID Card", "Found",
                "Canteen", "16-10-2026", "Available"},
            {"Java Book", "Books", "Lost",
                "Classroom", "16-10-2026", "Missing"}
        };

        JTable table = new JTable(data, columns);

        JScrollPane scrollPane = new JScrollPane(table);

        add(scrollPane, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ViewItems().setVisible(true);
        });
    }
}
