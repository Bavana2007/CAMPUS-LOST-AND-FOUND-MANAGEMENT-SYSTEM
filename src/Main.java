import ui.LoginForm;
import ui.UITheme;

import javax.swing.*;

/**
 * Application Entry Point for CAMPUS-LOST-AND-FOUND.
 * Configures the Nimbus Look and Feel and opens the LoginForm.
 */
public class Main {

    public static void main(String[] args) {
        // Run Swing UI initialization on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            // Apply Nimbus theme with consistent fonts
            UITheme.applyLookAndFeel();

            // Open Login Form
            LoginForm loginForm = new LoginForm();
            loginForm.setVisible(true);
        });
    }
}
