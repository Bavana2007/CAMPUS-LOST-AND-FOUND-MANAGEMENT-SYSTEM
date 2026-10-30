package ui;

import dao.ClaimDAO;
import dao.ItemDAO;
import model.Claim;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Main application window featuring role-based dashboards for Students and Administrators.
 */
public class MainMenu extends JFrame {

    private final User currentUser;
    private JTabbedPane tabbedPane;
    private ViewItems viewItemsPanel;
    private AdminClaimsPanel adminClaimsPanel;

    public MainMenu(User user) {
        this.currentUser = user;

        setTitle("Campus Lost and Found System - " + (user.isAdmin() ? "Admin Portal" : "Student Portal"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 680);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UITheme.BG_LIGHT);

        // TOP NAVIGATION HEADER
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(UITheme.PRIMARY_COLOR);
        topHeader.setBorder(new EmptyBorder(12, 20, 12, 20));

        // System Title
        JLabel titleLabel = new JLabel("CAMPUS LOST & FOUND MANAGEMENT SYSTEM");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);

        // User badge & Logout
        JPanel userBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        userBox.setOpaque(false);

        JLabel userBadge = new JLabel("<html><font color='#ecf0f1'>Logged in as: </font><b>"
            + currentUser.getName() + "</b> (" + currentUser.getRole().toUpperCase() + ")</html>");
        userBadge.setFont(UITheme.BODY_FONT);
        userBadge.setForeground(Color.WHITE);
        userBox.add(userBadge);

        JButton logoutBtn = UITheme.createSecondaryButton("Sign Out");
        logoutBtn.addActionListener(e -> handleLogout());
        userBox.add(logoutBtn);

        topHeader.add(titleLabel, BorderLayout.WEST);
        topHeader.add(userBox, BorderLayout.EAST);
        rootPanel.add(topHeader, BorderLayout.NORTH);

        // MAIN CONTENT TABS
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.HEADER_FONT);

        viewItemsPanel = new ViewItems(this, currentUser);

        if (currentUser.isAdmin()) {
            // ADMIN TABS
            adminClaimsPanel = new AdminClaimsPanel(this);

            tabbedPane.addTab("Manage All Items", viewItemsPanel);
            tabbedPane.addTab("Review Pending Claims", adminClaimsPanel);
        } else {
            // STUDENT TABS
            tabbedPane.addTab("Browse Lost & Found", viewItemsPanel);
            tabbedPane.addTab("My Submitted Claims", buildStudentClaimsPanel());
        }

        rootPanel.add(tabbedPane, BorderLayout.CENTER);

        // FOOTER BAR
        JPanel footerBar = new JPanel(new BorderLayout());
        footerBar.setBackground(new Color(236, 240, 241));
        footerBar.setBorder(new EmptyBorder(6, 16, 6, 16));

        JLabel copyright = new JLabel("Campus Lost & Found System v1.0 • Java Swing & MySQL JDBC");
        copyright.setFont(UITheme.SMALL_FONT);
        copyright.setForeground(UITheme.TEXT_MUTED);
        footerBar.add(copyright, BorderLayout.WEST);

        JButton dbBtn = new JButton("Database Settings");
        dbBtn.setFont(UITheme.SMALL_FONT);
        dbBtn.addActionListener(e -> UITheme.showDatabaseConfigDialog(this));
        footerBar.add(dbBtn, BorderLayout.EAST);

        rootPanel.add(footerBar, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    /**
     * Builds claims panel for the logged in student to see status of their claims.
     */
    private JPanel buildStudentClaimsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        // Header
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel title = new JLabel("MY SUBMITTED CLAIMS & STATUS");
        title.setFont(UITheme.HEADER_FONT);
        head.add(title, BorderLayout.WEST);

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh Claims");
        head.add(refreshBtn, BorderLayout.EAST);
        panel.add(head, BorderLayout.NORTH);

        // Table
        String[] cols = {"Claim ID", "Item ID", "Item Title", "Category", "Location", "My Message", "Date Submitted", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setReorderingAllowed(false);

        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                String s = val != null ? val.toString() : "";
                if (!isSel) {
                    if ("pending".equalsIgnoreCase(s)) c.setForeground(UITheme.WARNING_COLOR);
                    else if ("approved".equalsIgnoreCase(s)) c.setForeground(UITheme.SUCCESS_COLOR);
                    else if ("rejected".equalsIgnoreCase(s)) c.setForeground(UITheme.DANGER_COLOR);
                    setFont(UITheme.BOLD_FONT);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        Runnable loadStudentClaims = () -> {
            model.setRowCount(0);
            try {
                ClaimDAO claimDAO = new ClaimDAO();
                List<Claim> claims = claimDAO.getClaimsByClaimant(currentUser.getUserId());
                for (Claim c : claims) {
                    model.addRow(new Object[]{
                        c.getClaimId(),
                        c.getItemId(),
                        c.getItemTitle(),
                        c.getItemCategory(),
                        c.getItemLocation(),
                        c.getMessage(),
                        c.getClaimDate() != null ? c.getClaimDate().toString().substring(0, 16) : "-",
                        c.getStatus().toUpperCase()
                    });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error fetching your claims: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        refreshBtn.addActionListener(e -> loadStudentClaims.run());
        loadStudentClaims.run();

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to sign out?",
            "Sign Out",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            new LoginForm().setVisible(true);
            this.dispose();
        }
    }
}
