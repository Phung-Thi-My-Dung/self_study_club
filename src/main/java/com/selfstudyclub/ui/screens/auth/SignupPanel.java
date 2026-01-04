package com.selfstudyclub.ui.screens.auth;

import com.selfstudyclub.service.AuthService;
import com.selfstudyclub.ui.UiUtil;

import javax.swing.*;
import java.awt.*;

/** Signup UI (creates MEMBER by default). */
public class SignupPanel extends JPanel {
    private final JTextField emailField = new JTextField(28);
    private final JPasswordField passField = new JPasswordField(28);
    private final JPasswordField pass2Field = new JPasswordField(28);
    private final JButton createBtn = new JButton("Create account");
    private final JButton backBtn = new JButton("Back");

    public SignupPanel(AuthService authService, Runnable onBack) {
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.anchor = GridBagConstraints.WEST;

        int r = 0;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Email"), g);
        g.gridx = 1; form.add(emailField, g);

        r++;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Password"), g);
        g.gridx = 1; form.add(passField, g);

        r++;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Confirm"), g);
        g.gridx = 1; form.add(pass2Field, g);

        r++;
        g.gridx = 1; g.gridy = r;
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.add(createBtn);
        actions.add(backBtn);
        form.add(actions, g);

        add(UiUtil.pad(form), BorderLayout.CENTER);

        createBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String p1 = new String(passField.getPassword());
            String p2 = new String(pass2Field.getPassword());

            if (email.isBlank() || p1.isBlank() || p2.isBlank()) {
                UiUtil.error(this, "All fields are required.");
                return;
            }
            if (!p1.equals(p2)) {
                UiUtil.error(this, "Passwords do not match.");
                return;
            }
            if (p1.length() < 6) {
                UiUtil.error(this, "Password must be at least 6 characters.");
                return;
            }
            try {
                authService.signupMember(email, p1);
                UiUtil.info(this, "Account created. Please login.");
                onBack.run();
            } catch (Exception ex) {
                UiUtil.error(this, ex.getMessage());
            }
        });

        backBtn.addActionListener(e -> onBack.run());
    }
}
