package com.selfstudyclub.ui.screens.auth;

import com.selfstudyclub.security.Session;
import com.selfstudyclub.service.AuthService;
import com.selfstudyclub.ui.UiUtil;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/** Login UI (simple + clean). */
public class LoginPanel extends JPanel {
    private final JTextField emailField = new JTextField(28);
    private final JPasswordField passField = new JPasswordField(28);
    private final JButton loginBtn = new JButton("Login");
    private final JButton signupBtn = new JButton("Sign up");

    public LoginPanel(AuthService authService, Consumer<Session> onLoginSuccess, Runnable onSignup) {
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
        g.gridx = 1; g.gridy = r;
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.add(loginBtn);
        actions.add(signupBtn);
        form.add(actions, g);

        add(UiUtil.pad(form), BorderLayout.CENTER);

        loginBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String pass = new String(passField.getPassword());
            if (email.isBlank() || pass.isBlank()) {
                UiUtil.error(this, "Email and password are required.");
                return;
            }
            try {
                Session s = authService.login(email, pass);
                onLoginSuccess.accept(s);
            } catch (Exception ex) {
                UiUtil.error(this, ex.getMessage());
            }
        });

        signupBtn.addActionListener(e -> onSignup.run());
    }
}
