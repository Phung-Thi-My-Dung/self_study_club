package com.selfstudyclub;

import com.formdev.flatlaf.FlatLightLaf;
import com.selfstudyclub.config.AppConfig;
import com.selfstudyclub.config.DataSourceProvider;
import com.selfstudyclub.dao.*;
import com.selfstudyclub.dao.jdbc.*;
import com.selfstudyclub.service.*;
import com.selfstudyclub.ui.MainFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * App entry point.
 */
public final class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            try {
                AppConfig cfg = AppConfig.load();
                DataSourceProvider dsp = new DataSourceProvider(cfg);

                // DAOs
                AccountDao accountDao = new JdbcAccountDao(dsp.dataSource());
                RoleDao roleDao = new JdbcRoleDao(dsp.dataSource());
                UserRoleDao userRoleDao = new JdbcUserRoleDao(dsp.dataSource());
                UserProfileDao profileDao = new JdbcUserProfileDao(dsp.dataSource());
                CourseDao courseDao = new JdbcCourseDao(dsp.dataSource());
                EnrollmentDao enrollmentDao = new JdbcEnrollmentDao(dsp.dataSource());
                TaskDao taskDao = new JdbcTaskDao(dsp.dataSource());
                TaskCompletionDao completionDao = new JdbcTaskCompletionDao(dsp.dataSource());
                NotificationDao notificationDao = new JdbcNotificationDao(dsp.dataSource());
                NotificationReadDao readDao = new JdbcNotificationReadDao(dsp.dataSource());
                AdminDashboardDao adminDao = new JdbcAdminDashboardDao(dsp.dataSource());
                LeaderboardDao leaderboardDao = new JdbcLeaderboardDao(dsp.dataSource());

                // Services
                AuthService authService = new AuthService(accountDao, roleDao, userRoleDao);
                ProfileService profileService = new ProfileService(profileDao);
                CourseService courseService = new CourseService(courseDao, enrollmentDao);
                TaskService taskService = new TaskService(taskDao);
                StreakService streakService = new StreakService(completionDao);
                NotificationService notificationService = new NotificationService(notificationDao, readDao);
                AdminDashboardService adminDashboardService = new AdminDashboardService(adminDao);
                LeaderboardService leaderboardService = new LeaderboardService(leaderboardDao);

                MainFrame frame = new MainFrame(cfg, authService, profileService, courseService,
                        taskService, streakService, notificationService, adminDashboardService, leaderboardService);
                frame.setVisible(true);
            } catch (Exception ex) {
                log.error("Startup error", ex);
                JOptionPane.showMessageDialog(null,
                        "Cannot start app: " + ex.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
