package com.selfstudyclub.ui;

import com.selfstudyclub.config.AppConfig;
import com.selfstudyclub.security.Session;
import com.selfstudyclub.security.SessionManager;
import com.selfstudyclub.service.*;
import com.selfstudyclub.ui.screens.admin.AdminDashboardPanel;
import com.selfstudyclub.ui.screens.auth.LoginPanel;
import com.selfstudyclub.ui.screens.auth.SignupPanel;
import com.selfstudyclub.ui.screens.common.LeaderboardPanel;
import com.selfstudyclub.ui.screens.common.NotificationsPanel;
import com.selfstudyclub.ui.screens.courses.CoursesPanel;
import com.selfstudyclub.ui.screens.member.MyProfilePanel;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/** Main window with role-based menu. */
public class MainFrame extends JFrame {
    private final AppConfig cfg;
    private final AuthService authService;
    private final ProfileService profileService;
    private final CourseService courseService;
    private final TaskService taskService;
    private final StreakService streakService;
    private final NotificationService notificationService;
    private final AdminDashboardService adminDashboardService;
    private final LeaderboardService leaderboardService;

    private final JLabel sessionLabel = new JLabel("Not logged in");
    private final JButton backBtn = new JButton("Back");
    private final JButton logoutBtn = new JButton("Logout");

    private final JPanel content = new JPanel(new CardLayout());
    private final DefaultListModel<MenuItem> menuModel = new DefaultListModel<>();
    private final JList<MenuItem> menuList = new JList<>(menuModel);
    private final NavStack nav;

    public MainFrame(AppConfig cfg,
                     AuthService authService,
                     ProfileService profileService,
                     CourseService courseService,
                     TaskService taskService,
                     StreakService streakService,
                     NotificationService notificationService,
                     AdminDashboardService adminDashboardService,
                     LeaderboardService leaderboardService) {
        this.cfg = Objects.requireNonNull(cfg);
        this.authService = authService;
        this.profileService = profileService;
        this.courseService = courseService;
        this.taskService = taskService;
        this.streakService = streakService;
        this.notificationService = notificationService;
        this.adminDashboardService = adminDashboardService;
        this.leaderboardService = leaderboardService;

        setTitle(cfg.appName());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);

        NavStack.CardLayoutLike layout = panel -> {
            String key = Integer.toHexString(System.identityHashCode(panel));
            content.add(panel, key);
            ((CardLayout) content.getLayout()).show(content, key);
            revalidate();
            repaint();
        };
        this.nav = new NavStack(content, layout);

        buildUi();
        showAuth();
    }

    private void buildUi() {
        JPanel header = new JPanel(new BorderLayout(10, 10));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(backBtn);
        left.add(logoutBtn);
        header.add(left, BorderLayout.WEST);
        header.add(sessionLabel, BorderLayout.CENTER);
        header.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        backBtn.addActionListener(e -> nav.back());
        logoutBtn.addActionListener(e -> { authService.logout(); showAuth(); });

        menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        menuList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            MenuItem item = menuList.getSelectedValue();
            if (item != null) item.action.run();
        });

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(menuList), content);
        split.setDividerLocation(230);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(header, BorderLayout.NORTH);
        getContentPane().add(split, BorderLayout.CENTER);

        UiUtil.bindEscToBack(getRootPane(), nav::back);
    }

    private void showAuth() {
        menuModel.clear();
        sessionLabel.setText("Not logged in");
        backBtn.setEnabled(false);
        logoutBtn.setEnabled(false);

        JPanel login = new LoginPanel(authService, session -> onLoginSuccess(session), this::showSignup);
        nav.resetTo(login);
        refreshBackButton();
    }

    private void showSignup() {
        nav.push(new SignupPanel(authService, this::showAuth));
        refreshBackButton();
    }

    private void onLoginSuccess(Session session) {
        SessionManager.set(session);
        logoutBtn.setEnabled(true);
        sessionLabel.setText("Logged in: " + session.email() + " | Roles: " + session.roles());

        rebuildMenu(session);

        if (session.isAdmin()) {
            nav.resetTo(new AdminDashboardPanel(cfg, adminDashboardService, nav));
        } else {
            nav.resetTo(new CoursesPanel(cfg, courseService, taskService, streakService, nav, false));
        }
        refreshBackButton();
    }

    private void rebuildMenu(Session session) {
        menuModel.clear();

        if (session.isAdmin()) {
            menuModel.addElement(new MenuItem("Admin Dashboard",
                    () -> nav.push(new AdminDashboardPanel(cfg, adminDashboardService, nav))));
            menuModel.addElement(new MenuItem("All Courses",
                    () -> nav.push(new CoursesPanel(cfg, courseService, taskService, streakService, nav, true))));
        } else {
            menuModel.addElement(new MenuItem("My Courses",
                    () -> nav.push(new CoursesPanel(cfg, courseService, taskService, streakService, nav, false))));
        }

        menuModel.addElement(new MenuItem("Leaderboard", () -> nav.push(new LeaderboardPanel(leaderboardService, nav))));
        menuModel.addElement(new MenuItem("Notifications", () -> nav.push(new NotificationsPanel(notificationService, nav))));
        menuModel.addElement(new MenuItem("My Profile", () -> nav.push(new MyProfilePanel(profileService, nav))));
    }

    private void refreshBackButton() {
        Timer t = new Timer(250, e -> backBtn.setEnabled(nav.canBack()));
        t.setRepeats(false);
        t.start();
    }

    private static class MenuItem {
        final String label;
        final Runnable action;
        MenuItem(String label, Runnable action) { this.label = label; this.action = action; }
        @Override public String toString() { return label; }
    }
}
