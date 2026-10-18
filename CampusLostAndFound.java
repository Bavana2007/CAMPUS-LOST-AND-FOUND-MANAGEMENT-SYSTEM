package campuslostandfound;
import javax.swing.*;
public class CampusLostAndFound {
    public static void main(String[] args) {
        JFrame frame=new JFrame("Campus Lost & Found Management System");
        JLabel title=new JLabel("Campus Lost & Found Management System",
        SwingConstants.CENTER
        );
        JButton addButton=new JButton("Add Item");
        addButton.addActionListener(e -> {
            new ItemForm().setVisible(true);
        });
        JButton viewButton=new JButton("View Items");
        viewButton.addActionListener(e -> {
            new ViewItems().setVisible(true);
        });
        JButton searchButton=new JButton("Search Items");
        JPanel panel=new JPanel();
        
        panel.add(title);
        panel.add(addButton);
        panel.add(viewButton);
        panel.add(searchButton);
        
        frame.add(panel);
        frame.setSize(600,300);
        
frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
}
