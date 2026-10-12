package gui;

import dao.UserDAO;
import model.User;
import web.SecurityUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("Bus Reservation System - Login");
        setSize(520, 440);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("Bus Reservation System");
        title.setBounds(100, 30, 350, 35);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(44, 62, 80));

        JLabel subtitle = new JLabel("Please enter your credentials to login");
        subtitle.setBounds(135, 65, 300, 20);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitle.setForeground(new Color(127, 140, 141));

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(70, 115, 100, 30);
        userLabel.setFont(new Font("Arial", Font.BOLD, 13));

        usernameField = new JTextField();
        usernameField.setBounds(170, 115, 250, 32);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(70, 165, 100, 30);
        passLabel.setFont(new Font("Arial", Font.BOLD, 13));

        passwordField = new JPasswordField();
        passwordField.setBounds(170, 165, 250, 32);

        JButton loginButton = new JButton("Sign In");
        loginButton.setBounds(140, 225, 120, 38);
        loginButton.setBackground(new Color(41, 128, 185));
        loginButton.setForeground(Color.BLACK);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton registerButton = new JButton("➕ Register");
        registerButton.setBounds(275, 225, 145, 38);
        registerButton.setBackground(new Color(39, 174, 96));
        registerButton.setForeground(Color.BLACK);
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel helperText = new JLabel("Sign in with your credentials or register a new traveler account", SwingConstants.CENTER);
        helperText.setBounds(20, 285, 480, 20);
        helperText.setFont(new Font("Arial", Font.PLAIN, 11));
        helperText.setForeground(new Color(127, 140, 141));

        // Copyright Notice
        JLabel copyrightLabel = new JLabel("© 2026 SwiftBus Inc. All Rights Reserved.", SwingConstants.CENTER);
        copyrightLabel.setBounds(20, 360, 480, 25);
        copyrightLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        copyrightLabel.setForeground(new Color(149, 165, 166));

        ActionListener loginAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String identifier = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());

                if (identifier.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Please enter both username and password.", "Missing Fields", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Hardcoded fallback for admin
                if (identifier.equalsIgnoreCase("admin") && password.equals("1234")) {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Login Successful! Welcome Administrator.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    new Dashboard().setVisible(true);
                    dispose();
                    return;
                }

                // Authenticate against database
                try {
                    UserDAO userDAO = new UserDAO();
                    User user = userDAO.findByUsernameOrEmail(identifier);
                    int iterations = SecurityUtils.getPbkdf2Iterations();
                    if (user != null && SecurityUtils.verifyPassword(password.toCharArray(), user.getSalt(), user.getPasswordHash(), iterations)) {
                        JOptionPane.showMessageDialog(LoginFrame.this, "Login Successful! Welcome " + user.getUsername() + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
                        new Dashboard().setVisible(true);
                        dispose();
                        return;
                    }
                } catch (Exception ex) {
                    System.err.println("[LoginFrame] DB Auth Error: " + ex.getMessage());
                }

                JOptionPane.showMessageDialog(LoginFrame.this, "Invalid Username or Password!", "Authentication Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        loginButton.addActionListener(loginAction);
        usernameField.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);

        registerButton.addActionListener(e -> showRegisterDialog());

        panel.add(title);
        panel.add(subtitle);
        panel.add(userLabel);
        panel.add(usernameField);
        panel.add(passLabel);
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);
        panel.add(helperText);
        panel.add(copyrightLabel);

        add(panel);
    }

    private void showRegisterDialog() {
        JDialog dialog = new JDialog(this, "Register New Account - SwiftBus", true);
        dialog.setSize(460, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 12));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));

        JTextField regUserField = new JTextField();
        JTextField regEmailField = new JTextField();
        JPasswordField regPassField = new JPasswordField();
        JPasswordField regConfirmField = new JPasswordField();

        formPanel.add(new JLabel("Username (3-30 chars):"));
        formPanel.add(regUserField);

        formPanel.add(new JLabel("Email Address:"));
        formPanel.add(regEmailField);

        formPanel.add(new JLabel("Password (min 8 chars):"));
        formPanel.add(regPassField);

        formPanel.add(new JLabel("Confirm Password:"));
        formPanel.add(regConfirmField);

        JLabel policyLabel = new JLabel("<html><small style='color:#e67e22;'>Policy: Min 8 chars &amp; 1 special symbol (!@#$%^&amp;*)</small></html>");
        formPanel.add(new JLabel("Password Policy:"));
        formPanel.add(policyLabel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        JButton cancelBtn = new JButton("Cancel");
        JButton submitBtn = new JButton("Register Account");
        submitBtn.setBackground(new Color(39, 174, 96));
        submitBtn.setForeground(Color.BLACK);
        submitBtn.setFont(new Font("Arial", Font.BOLD, 12));

        cancelBtn.addActionListener(ev -> dialog.dispose());

        submitBtn.addActionListener(ev -> {
            String u = regUserField.getText().trim();
            String em = regEmailField.getText().trim();
            String pwd = new String(regPassField.getPassword());
            String cpwd = new String(regConfirmField.getPassword());

            if (!u.matches("^[a-zA-Z0-9_]{3,30}$")) {
                JOptionPane.showMessageDialog(dialog, "Username must be 3-30 characters (letters, numbers, underscore only).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!em.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid email address.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!SecurityUtils.meetsPasswordPolicy(pwd)) {
                JOptionPane.showMessageDialog(dialog, "Password must be at least 8 characters long and contain at least one special character (!@#$%^&* etc.).", "Password Policy Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!pwd.equals(cpwd)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            UserDAO dao = new UserDAO();
            if (dao.isUsernameTaken(u)) {
                JOptionPane.showMessageDialog(dialog, "Username is already taken. Please choose another.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (dao.isEmailTaken(em)) {
                JOptionPane.showMessageDialog(dialog, "Email is already registered. Please sign in or use another email.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            byte[] salt = SecurityUtils.generateRandomSalt();
            String saltHex = SecurityUtils.bytesToHex(salt);
            int iterations = SecurityUtils.getPbkdf2Iterations();
            String hashHex = SecurityUtils.hashPassword(pwd.toCharArray(), salt, iterations);
            User newUser = new User(u, em, hashHex, saltHex, "USER");

            if (dao.createUser(newUser)) {
                JOptionPane.showMessageDialog(dialog, "Account registered successfully! You can now log in.", "Registration Success", JOptionPane.INFORMATION_MESSAGE);
                usernameField.setText(u);
                passwordField.setText("");
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Database error creating user account. Please ensure MySQL is running.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(submitBtn);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JLabel regCopy = new JLabel("© 2026 SwiftBus Inc. All Rights Reserved.", SwingConstants.CENTER);
        regCopy.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        regCopy.setFont(new Font("Arial", Font.PLAIN, 10));
        regCopy.setForeground(new Color(149, 165, 166));
        bottomPanel.add(btnPanel, BorderLayout.CENTER);
        bottomPanel.add(regCopy, BorderLayout.SOUTH);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}