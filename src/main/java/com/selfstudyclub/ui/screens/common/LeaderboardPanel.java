package com.selfstudyclub.ui.screens.common;

import com.selfstudyclub.model.LeaderboardRow;
import com.selfstudyclub.service.LeaderboardService;
import com.selfstudyclub.ui.NavStack;
import com.selfstudyclub.ui.UiUtil;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

/** Leaderboard screen (Top 50). */
public class LeaderboardPanel extends JPanel {
    private final LeaderboardService service;
    private final NavStack nav;

    private final JTable table = new JTable();
    private final JComboBox<String> type = new JComboBox<>(new String[]{"Total streak count", "Total active days"});

    public LeaderboardPanel(LeaderboardService service, NavStack nav) {
        this.service = service;
        this.nav = nav;

        setLayout(new BorderLayout(10,10));
        UiUtil.bindEscToBack(this, nav::back);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel t = new JLabel("Leaderboard (Top 50)");
        t.setFont(t.getFont().deriveFont(Font.BOLD, 16f));
        top.add(t);
        top.add(Box.createHorizontalStrut(12));
        top.add(type);

        type.addActionListener(e -> reload());

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        reload();
    }

    private void reload() {
        try {
            List<LeaderboardRow> rows = type.getSelectedIndex() == 0
                    ? service.topByTotalCompletions()
                    : service.topByActiveDays();
            table.setModel(new LbModel(rows));
        } catch (Exception ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    static class LbModel extends AbstractTableModel {
        final List<LeaderboardRow> rows;
        private final String[] cols = {"Rank", "Account ID", "Email", "Full name", "Value"};

        LbModel(List<LeaderboardRow> rows) { this.rows = rows; }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override public Object getValueAt(int r, int c) {
            LeaderboardRow row = rows.get(r);
            return switch (c) {
                case 0 -> r + 1;
                case 1 -> row.accountId();
                case 2 -> row.email();
                case 3 -> (row.fullName() == null ? "" : row.fullName());
                case 4 -> row.value();
                default -> "";
            };
        }
    }
}
