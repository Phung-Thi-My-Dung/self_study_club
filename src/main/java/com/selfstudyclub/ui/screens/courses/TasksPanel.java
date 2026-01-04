package com.selfstudyclub.ui.screens.courses;

import com.selfstudyclub.config.AppConfig;
import com.selfstudyclub.model.Course;
import com.selfstudyclub.model.Task;
import com.selfstudyclub.security.Session;
import com.selfstudyclub.security.SessionManager;
import com.selfstudyclub.service.StreakService;
import com.selfstudyclub.service.TaskService;
import com.selfstudyclub.ui.NavStack;
import com.selfstudyclub.ui.UiUtil;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

/** Tasks screen (double-click a task to create completion today). */
public class TasksPanel extends JPanel {
    private final AppConfig cfg;
    private final Course course;
    private final TaskService taskService;
    private final StreakService streakService;
    private final NavStack nav;

    private final JTable table = new JTable();
    private int page = 1;

    public TasksPanel(AppConfig cfg, Course course, TaskService taskService, StreakService streakService, NavStack nav) {
        this.cfg = cfg;
        this.course = course;
        this.taskService = taskService;
        this.streakService = streakService;
        this.nav = nav;

        setLayout(new BorderLayout(10,10));
        UiUtil.bindEscToBack(this, nav::back);

        add(buildTopBar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        reload();
        bindDoubleClickToComplete();
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel t = new JLabel("Course: " + course.title() + " (double-click task to complete today)");
        t.setFont(t.getFont().deriveFont(Font.BOLD, 16f));
        p.add(t);

        JButton create = new JButton("Create task");
        create.addActionListener(e -> openCreateDialog());
        p.add(Box.createHorizontalStrut(12));
        p.add(create);
        return p;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton prev = new JButton("Prev");
        JButton next = new JButton("Next");
        p.add(prev); p.add(next);
        prev.addActionListener(e -> { if (page > 1) { page--; reload(); } });
        next.addActionListener(e -> { page++; reload(); });
        return p;
    }

    private void reload() {
        try {
            List<Task> rows = taskService.listByCourse(course.courseId(), page, cfg.pageSize());
            table.setModel(new TaskModel(rows));
        } catch (Exception ex) {
            if (page > 1) page--;
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void openCreateDialog() {
        Session s = SessionManager.current().orElseThrow();

        JTextField title = new JTextField(24);
        JTextArea desc = new JTextArea(6,24);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.anchor = GridBagConstraints.WEST;
        g.gridx=0; g.gridy=0; form.add(new JLabel("Title"), g);
        g.gridx=1; form.add(title, g);
        g.gridx=0; g.gridy=1; form.add(new JLabel("Description"), g);
        g.gridx=1; form.add(new JScrollPane(desc), g);

        int ok = JOptionPane.showConfirmDialog(this, form, "Create task", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        if (title.getText().trim().isBlank()) {
            UiUtil.error(this, "Title is required.");
            return;
        }

        try {
            taskService.create(course.courseId(), title.getText().trim(), desc.getText().trim(), s.accountId());
            UiUtil.info(this, "Task created.");
            reload();
        } catch (Exception ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void bindDoubleClickToComplete() {
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row < 0) return;
                    Task t = ((TaskModel) table.getModel()).rows.get(row);
                    completeToday(t);
                }
            }
        });
    }

    private void completeToday(Task t) {
        Session s = SessionManager.current().orElseThrow();
        String note = JOptionPane.showInputDialog(this, "Note (optional):", "");
        try {
            streakService.submitCompletionToday(s.accountId(), t.taskId(), note);
            UiUtil.info(this, "Completed! (streak event saved)");
        } catch (Exception ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    static class TaskModel extends AbstractTableModel {
        final List<Task> rows;
        private final String[] cols = {"Task ID", "Title", "Description"};
        TaskModel(List<Task> rows) { this.rows = rows; }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override public Object getValueAt(int r, int c) {
            Task row = rows.get(r);
            return switch (c) {
                case 0 -> row.taskId();
                case 1 -> row.title();
                case 2 -> row.description();
                default -> "";
            };
        }
    }
}
