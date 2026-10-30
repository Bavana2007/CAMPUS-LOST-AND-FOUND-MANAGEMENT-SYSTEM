package ui;

import dao.UserDAO;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * Authentication window: Login and Registration with role selection.
 */
public class LoginForm extends JFrame {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    private final UserDAO userDAO = new UserDAO();

    // Login components
    private JTextField loginEmailField;
    private JPasswordField loginPasswordField;

    // Register components
    private JTextField regNameField;
    private JTextField regEmailField;
    private JPasswordField regPasswordField;
    private JPasswordField regConfirmPasswordField;
    private JComboBox<String> regRoleCombo;

    private JTabbedPane tabbedPane;

    public LoginForm() {
        setTitle("Campus Lost and Found - Authentication");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 520);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(16, 20, 16, 20));
        mainPanel.setBackground(UITheme.BG_LIGHT);

        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel titleLabel = new JLabel("CAMPUS LOST AND FOUND", SwingConstants.CENTER);
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Student & Admin Portal", SwingConstants.CENTER);
        subtitleLabel.setFont(UITheme.BODY_FONT);
        subtitleLabel.setForeground(new Color(220, 230, 245));

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Tabbed Pane for Login / Register
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.HEADER_FONT);
        tabbedPane.addTab("Sign In", buildLoginPanel());
        tabbedPane.addTab("Register New Account", buildRegisterPanel());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Footer with DB Settings shortcut
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(UITheme.BG_LIGHT);
        JButton dbConfigBtn = new JButton("Database Settings");
        dbConfigBtn.setFont(UITheme.SMALL_FONT);
        dbConfigBtn.addActionListener(e -> UITheme.showDatabaseConfigDialog(this));
        footerPanel.add(dbConfigBtn);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Email
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel emailLbl = new JLabel("Campus Email:");
        emailLbl.setFont(UITheme.BOLD_FONT);
        panel.add(emailLbl, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        loginEmailField = new JTextField(20);
        loginEmailField.setText("admin@campus.edu"); // Helpful default demo
        panel.add(loginEmailField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(UITheme.BOLD_FONT);
        panel.add(passLbl, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7;
        loginPasswordField = new JPasswordField(20);
        loginPasswordField.setText("admin123"); // Helpful default demo
        panel.add(loginPasswordField, gbc);

        // Quick hint
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JLabel hintLabel = new JLabel("<html><font color='#7f8c8d'>Demo logins: <b>admin@campus.edu</b> / admin123<br>or <b>alice@student.campus.edu</b> / student123</font></html>");
        hintLabel.setFont(UITheme.SMALL_FONT);
        panel.add(hintLabel, gbc);

        // Submit Button
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JButton loginBtn = UITheme.createPrimaryButton("Sign In to Portal");
        loginBtn.addActionListener(e -> handleLogin());
        panel.add(loginBtn, gbc);

        // Enter key submits login
        loginPasswordField.addActionListener(e -> handleLogin());
        loginEmailField.addActionListener(e -> handleLogin());

        return panel;
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Full Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        JLabel nameLbl = new JLabel("Full Name:");
        nameLbl.setFont(UITheme.BOLD_FONT);
        panel.add(nameLbl, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.65;
        regNameField = new JTextField(18);
        panel.add(regNameField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel emailLbl = new JLabel("Email:");
        emailLbl.setFont(UITheme.BOLD_FONT);
        panel.add(emailLbl, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        regEmailField = new JTextField(18);
        panel.add(regEmailField, gbc);

        // Role
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel roleLbl = new JLabel("Account Role:");
        roleLbl.setFont(UITheme.BOLD_FONT);
        panel.add(roleLbl, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        regRoleCombo = new JComboBox<>(new String[]{"student", "admin"});
        panel.add(regRoleCombo, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(UITheme.BOLD_FONT);
        panel.add(passLbl, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        regPasswordField = new JPasswordField(18);
        panel.add(regPasswordField, gbc);

        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel confirmLbl = new JLabel("Confirm Password:");
        confirmLbl.setFont(UITheme.BOLD_FONT);
        panel.add(confirmLbl, gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        regConfirmPasswordField = new JPasswordField(18);
        panel.add(regConfirmPasswordField, gbc);

        // Submit Button
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JButton regBtn = UITheme.createPrimaryButton("Create Account");
        regBtn.addActionListener(e -> handleRegister());
        panel.add(regBtn, gbc);

        return panel;
    }

    private void handleLogin() {
        String email = loginEmailField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your email.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            loginEmailField.requestFocus();
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            loginEmailField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            loginPasswordField.requestFocus();
            return;
        }

        try {
            User user = userDAO.login(email, password);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Invalid email or password. Please try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Open Main Menu with authenticated user
            MainMenu mainMenu = new MainMenu(user);
            mainMenu.setVisible(true);
            this.dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Database Connection Error: " + ex.getMessage() + "\n\nPlease check your MySQL server, credentials, and database.sql execution.",
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void handleRegister() {
        String name = regNameField.getText().trim();
        String email = regEmailField.getText().trim();
        String role = (String) regRoleCombo.getSelectedItem();
        String pass = new String(regPasswordField.getPassword());
        String confirm = new String(regConfirmPasswordField.getPassword());

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            regNameField.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            regEmailField.requestFocus();
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address format (e.g. name@campus.edu).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            regEmailField.requestFocus();
            return;
        }

        if (pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            regPasswordField.requestFocus();
            return;
        }

        if (pass.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password should be at least 4 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            regPasswordField.requestFocus();
            return;
        }

        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            regConfirmPasswordField.requestFocus();
            return;
        }

        try {
            if (userDAO.emailExists(email)) {
                JOptionPane.showMessageDialog(this, "This email is already registered. Please sign in instead.", "Account Exists", JOptionPane.WARNING_MESSAGE);
                return;
            }

            User newUser = new User(name, email, pass, role);
            boolean created = userDAO.register(newUser);
            if (created) {
                JOptionPane.showMessageDialog(this, "Account successfully created! You may now sign in.", "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
                loginEmailField.setText(email);
                loginPasswordField.setText(pass);
                regNameField.setText("");
                regEmailField.setText("");
                regPasswordField.setText("");
                regConfirmPasswordField.setText("");
                tabbedPane.setSelectedIndex(0);
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
