package com.selfstudyclub.ui.screens.admin;

import com.selfstudyclub.config.AppConfig;
import com.selfstudyclub.model.DashboardStats;
import com.selfstudyclub.model.MemberRow;
import com.selfstudyclub.model.TopCourseRow;
import com.selfstudyclub.service.AdminDashboardService;
import com.selfstudyclub.ui.NavStack;
import com.selfstudyclub.ui.UiUtil;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

/** Admin Dashboard screen (global stats). */
public class AdminDashboardPanel extends JPanel {
    private final AppConfig cfg;
    private final AdminDashboardService service;
    private final NavStack nav;

    private final JLabel totalMembers = new JLabel("-");
    private final JLabel membersToday = new JLabel("-");
    private final JLabel totalCourses = new JLabel("-");

    private final JTable topCoursesTable = new JTable();
    private final JTable membersTable = new JTable();
    private int page = 1;

    public AdminDashboardPanel(AppConfig cfg, AdminDashboardService service, NavStack nav) {
        this.cfg = cfg;
        this.service = service;
        this.nav = nav;

        setLayout(new BorderLayout(10,10));
        UiUtil.bindEscToBack(this, nav::back);

        add(buildCards(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        reload();
    }

    private JPanel buildCards() {
        JPanel p = new JPanel(new GridLayout(1, 3, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
        p.add(card("Total members", totalMembers));
        p.add(card("Members active today", membersToday));
        p.add(card("Total courses", totalCourses));
        return p;
    }

    private JPanel card(String title, JLabel value) {
        JPanel c = new JPanel(new BorderLayout());
        c.setBorder(BorderFactory.createTitledBorder(title));
        value.setFont(value.getFont().deriveFont(Font.BOLD, 22f));
        c.add(value, BorderLayout.CENTER);
        return c;
    }

    private JPanel buildCenter() {
        JPanel wrap = new JPanel(new BorderLayout(10,10));
        wrap.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel grids = new JPanel(new GridLayout(1, 2, 10, 10));
        grids.add(panelWithTable("Top 5 courses today", topCoursesTable));
        grids.add(panelWithTable("Active members today (paged)", membersTable));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton prev = new JButton("Prev");
        JButton next = new JButton("Next");
        footer.add(prev);
        footer.add(next);
        prev.addActionListener(e -> { if (page > 1) { page--; reloadMembers(); } });
        next.addActionListener(e -> { page++; reloadMembers(); });

        wrap.add(grids, BorderLayout.CENTER);
        wrap.add(footer, BorderLayout.SOUTH);
        return wrap;
    }

    private JPanel panelWithTable(String title, JTable table) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void reload() {
        try {
            DashboardStats s = service.stats();
            totalMembers.setText(String.valueOf(s.totalMembers()));
            membersToday.setText(String.valueOf(s.membersActiveToday()));
            totalCourses.setText(String.valueOf(s.totalCourses()));

            List<TopCourseRow> top = service.top5CoursesToday();
            topCoursesTable.setModel(new TopCoursesModel(top));
            reloadMembers();
        } catch (Exception ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void reloadMembers() {
        try {
            List<MemberRow> rows = service.membersActiveToday(page, cfg.pageSize());
            membersTable.setModel(new MembersModel(rows));
        } catch (Exception ex) {
            if (page > 1) page--;
            UiUtil.error(this, ex.getMessage());
        }
    }

    private static class TopCoursesModel extends AbstractTableModel {
        private final List<TopCourseRow> rows;
        private final String[] cols = {"Course ID", "Title", "Completions"};
        TopCoursesModel(List<TopCourseRow> rows) { this.rows = rows; }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override public Object getValueAt(int r, int c) {
            TopCourseRow row = rows.get(r);
            return switch (c) {
                case 0 -> row.courseId();
                case 1 -> row.title();
                case 2 -> row.completionsToday();
                default -> "";
            };
        }
    }

    private static class MembersModel extends AbstractTableModel {
        private final List<MemberRow> rows;
        private final String[] cols = {"Account ID", "Email", "Full name"};
        MembersModel(List<MemberRow> rows) { this.rows = rows; }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override public Object getValueAt(int r, int c) {
            MemberRow row = rows.get(r);
            return switch (c) {
                case 0 -> row.accountId();
                case 1 -> row.email();
                case 2 -> (row.fullName() == null ? "" : row.fullName());
                default -> "";
            };
        }
    }
}
