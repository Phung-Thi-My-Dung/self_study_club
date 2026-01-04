package com.selfstudyclub.ui.screens.common;

import com.selfstudyclub.model.Notification;
import com.selfstudyclub.security.Session;
import com.selfstudyclub.security.SessionManager;
import com.selfstudyclub.service.NotificationService;
import com.selfstudyclub.ui.NavStack;
import com.selfstudyclub.ui.UiUtil;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;
import java.util.Set;

/** Notifications screen. */
public class NotificationsPanel extends JPanel {
    private final NotificationService service;
    private final NavStack nav;
    private final JTable table = new JTable();

    public NotificationsPanel(NotificationService service, NavStack nav) {
        this.service = service;
        this.nav = nav;

        setLayout(new BorderLayout(10,10));
        UiUtil.bindEscToBack(this, nav::back);

        add(buildTop(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        reload();
        bindDoubleClick();
    }

    private JPanel buildTop() {
        Session s = SessionManager.current().orElseThrow();
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel t = new JLabel("Notifications (double-click to open)");
        t.setFont(t.getFont().deriveFont(Font.BOLD, 16f));
        p.add(t);

        if (s.isAdmin()) {
            JButton create = new JButton("Create notification");
            create.addActionListener(e -> openCreateDialog());
            p.add(Box.createHorizontalStrut(12));
            p.add(create);
        }
        return p;
    }

    private void reload() {
        Session s = SessionManager.current().orElseThrow();
        try {
            List<Notification> rows = service.latest(200);
            Set<Long> read = service.readIds(s.accountId());
            table.setModel(new NotifModel(rows, read));
        } catch (Exception ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void bindDoubleClick() {
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row < 0) return;
                    Notification n = ((NotifModel) table.getModel()).rows.get(row);
                    openAndMarkRead(n);
                }
            }
        });
    }

    private void openAndMarkRead(Notification n) {
        Session s = SessionManager.current().orElseThrow();

        JTextArea body = new JTextArea(n.body(), 18, 60);
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);

        JPanel p = new JPanel(new BorderLayout(8,8));
        p.add(new JLabel("Title: " + n.title()), BorderLayout.NORTH);
        p.add(new JScrollPane(body), BorderLayout.CENTER);
        String link = (n.linkUrl() == null || n.linkUrl().isBlank()) ? "(none)" : n.linkUrl();
        p.add(new JLabel("Link: " + link), BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, p, "Notification", JOptionPane.INFORMATION_MESSAGE);

        try {
            service.markRead(n.notificationId(), s.accountId());
            reload();
        } catch (Exception ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void openCreateDialog() {
        Session s = SessionManager.current().orElseThrow();

        JTextField title = new JTextField(26);
        JTextField link = new JTextField(26);
        JTextArea body = new JTextArea(10, 26);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.anchor = GridBagConstraints.WEST;

        int r = 0;
        g.gridx=0; g.gridy=r; form.add(new JLabel("Title"), g);
        g.gridx=1; form.add(title, g);

        r++;
        g.gridx=0; g.gridy=r; form.add(new JLabel("Link (optional)"), g);
        g.gridx=1; form.add(link, g);

        r++;
        g.gridx=0; g.gridy=r; form.add(new JLabel("Body"), g);
        g.gridx=1; form.add(new JScrollPane(body), g);

        int ok = JOptionPane.showConfirmDialog(this, form, "Create Notification", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        if (title.getText().trim().isBlank() || body.getText().trim().isBlank()) {
            UiUtil.error(this, "Title and body are required.");
            return;
        }

        try {
            service.create(title.getText().trim(), body.getText().trim(), link.getText().trim(), s.accountId());
            UiUtil.info(this, "Notification created.");
            reload();
        } catch (Exception ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    static class NotifModel extends AbstractTableModel {
        final List<Notification> rows;
        final Set<Long> readIds;
        private final String[] cols = {"Read", "Title", "Created At", "Link"};

        NotifModel(List<Notification> rows, Set<Long> readIds) { this.rows = rows; this.readIds = readIds; }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override public Object getValueAt(int r, int c) {
            Notification n = rows.get(r);
            boolean read = readIds.contains(n.notificationId());
            return switch (c) {
                case 0 -> read ? "✓" : "";
                case 1 -> n.title();
                case 2 -> n.createdAt();
                case 3 -> (n.linkUrl() == null ? "" : n.linkUrl());
                default -> "";
            };
        }
    }
}
