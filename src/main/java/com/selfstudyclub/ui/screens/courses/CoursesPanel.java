package com.selfstudyclub.ui.screens.courses;

import com.selfstudyclub.config.AppConfig;
import com.selfstudyclub.model.Course;
import com.selfstudyclub.security.Session;
import com.selfstudyclub.security.SessionManager;
import com.selfstudyclub.service.CourseService;
import com.selfstudyclub.service.StreakService;
import com.selfstudyclub.service.TaskService;
import com.selfstudyclub.ui.NavStack;
import com.selfstudyclub.ui.UiUtil;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

/** Courses screen (double-click a row to open tasks). */
public class CoursesPanel extends JPanel {
    private final AppConfig cfg;
    private final CourseService courseService;
    private final TaskService taskService;
    private final StreakService streakService;
    private final NavStack nav;
    private final boolean adminMode;

    private final JTable table = new JTable();
    private int page = 1;

    public CoursesPanel(AppConfig cfg, CourseService courseService, TaskService taskService,
                        StreakService streakService, NavStack nav, boolean adminMode) {
        this.cfg = cfg;
        this.courseService = courseService;
        this.taskService = taskService;
        this.streakService = streakService;
        this.nav = nav;
        this.adminMode = adminMode;

        setLayout(new BorderLayout(10,10));
        UiUtil.bindEscToBack(this, nav::back);

        add(buildTopBar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        reload();
        bindDoubleClick();
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel t = new JLabel(adminMode ? "All Courses" : "My Courses");
        t.setFont(t.getFont().deriveFont(Font.BOLD, 16f));
        p.add(t);

        if (!adminMode) {
            JButton create = new JButton("Create course");
            create.addActionListener(e -> openCreateDialog());
            p.add(Box.createHorizontalStrut(12));
            p.add(create);
        }
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
        Session s = SessionManager.current().orElseThrow();
        try {
            List<Course> rows = adminMode
                    ? courseService.listAll(page, cfg.pageSize())
                    : courseService.listMine(s.accountId(), page, cfg.pageSize());
            table.setModel(new CoursesModel(rows));
        } catch (Exception ex) {
            if (page > 1) page--;
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void bindDoubleClick() {
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row < 0) return;
                    Course c = ((CoursesModel) table.getModel()).rows.get(row);
                    nav.push(new TasksPanel(cfg, c, taskService, streakService, nav));
                }
            }
        });
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

        int ok = JOptionPane.showConfirmDialog(this, form, "Create course", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        if (title.getText().trim().isBlank()) {
            UiUtil.error(this, "Title is required.");
            return;
        }

        try {
            courseService.createCourseAndEnroll(title.getText().trim(), desc.getText().trim(), s.accountId());
            UiUtil.info(this, "Course created.");
            reload();
        } catch (Exception ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    static class CoursesModel extends AbstractTableModel {
        final List<Course> rows;
        private final String[] cols = {"Course ID", "Title", "Description", "Creator"};
        CoursesModel(List<Course> rows) { this.rows = rows; }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override public Object getValueAt(int r, int c) {
            Course row = rows.get(r);
            return switch (c) {
                case 0 -> row.courseId();
                case 1 -> row.title();
                case 2 -> row.description();
                case 3 -> row.createdBy();
                default -> "";
            };
        }
    }
}
