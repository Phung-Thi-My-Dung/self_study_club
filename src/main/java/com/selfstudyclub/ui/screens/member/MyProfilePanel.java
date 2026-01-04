package com.selfstudyclub.ui.screens.member;

import com.selfstudyclub.model.UserProfile;
import com.selfstudyclub.security.Session;
import com.selfstudyclub.security.SessionManager;
import com.selfstudyclub.service.ProfileService;
import com.selfstudyclub.ui.NavStack;
import com.selfstudyclub.ui.UiUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/** Member profile screen (self-edit). */
public class MyProfilePanel extends JPanel {
    private final ProfileService service;
    private final NavStack nav;

    private final JTextField fullName = new JTextField(24);
    private final JTextField dob = new JTextField(24); // yyyy-MM-dd
    private final JComboBox<String> gender = new JComboBox<>(new String[]{"", "MALE", "FEMALE", "OTHER"});
    private final JTextField phone = new JTextField(24);
    private final JTextField address = new JTextField(24);
    private final JTextArea bio = new JTextArea(6, 24);
    private final JButton saveBtn = new JButton("Save");

    public MyProfilePanel(ProfileService service, NavStack nav) {
        this.service = service;
        this.nav = nav;

        setLayout(new BorderLayout());
        UiUtil.bindEscToBack(this, nav::back);

        bio.setLineWrap(true);
        bio.setWrapStyleWord(true);

        add(UiUtil.pad(buildForm()), BorderLayout.CENTER);
        load();
        saveBtn.addActionListener(e -> save());
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.anchor = GridBagConstraints.WEST;

        int r = 0;
        row(form, g, r++, "Full name", fullName);
        row(form, g, r++, "Date of birth (yyyy-MM-dd)", dob);
        row(form, g, r++, "Gender", gender);
        row(form, g, r++, "Phone", phone);
        row(form, g, r++, "Address", address);

        g.gridx=0; g.gridy=r; form.add(new JLabel("Bio"), g);
        g.gridx=1; form.add(new JScrollPane(bio), g);
        r++;

        g.gridx=1; g.gridy=r; form.add(saveBtn, g);
        return form;
    }

    private void row(JPanel form, GridBagConstraints g, int r, String label, Component field) {
        g.gridx=0; g.gridy=r; form.add(new JLabel(label), g);
        g.gridx=1; form.add(field, g);
    }

    private void load() {
        Session s = SessionManager.current().orElseThrow();
        service.get(s.accountId()).ifPresent(p -> {
            fullName.setText(nvl(p.fullName()));
            dob.setText(p.dateOfBirth() == null ? "" : p.dateOfBirth().toString());
            gender.setSelectedItem(p.gender() == null ? "" : p.gender());
            phone.setText(nvl(p.phone()));
            address.setText(nvl(p.address()));
            bio.setText(nvl(p.bio()));
        });
    }

    private void save() {
        Session s = SessionManager.current().orElseThrow();

        LocalDate dobValue = null;
        if (!dob.getText().trim().isBlank()) {
            try { dobValue = LocalDate.parse(dob.getText().trim()); }
            catch (Exception ex) {
                UiUtil.error(this, "Invalid date format. Use yyyy-MM-dd.");
                return;
            }
        }

        UserProfile p = new UserProfile(
                s.accountId(),
                nullIfEmpty(fullName.getText()),
                dobValue,
                nullIfEmpty((String) gender.getSelectedItem()),
                nullIfEmpty(phone.getText()),
                nullIfEmpty(address.getText()),
                nullIfEmpty(bio.getText()),
                null,
                null
        );

        try {
            service.save(p);
            UiUtil.info(this, "Saved.");
        } catch (Exception ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private String nvl(String s) { return s == null ? "" : s; }
    private String nullIfEmpty(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
