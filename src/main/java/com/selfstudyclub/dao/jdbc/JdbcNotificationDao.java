package com.selfstudyclub.dao.jdbc;

import com.selfstudyclub.dao.DaoException;
import com.selfstudyclub.dao.NotificationDao;
import com.selfstudyclub.model.Notification;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcNotificationDao implements NotificationDao {
    private final DataSource ds;
    public JdbcNotificationDao(DataSource ds) { this.ds = ds; }

    @Override
    public long createNotification(String title, String body, String linkUrl, long createdBy) {
        String sql = "INSERT INTO notifications(title,body,link_url,created_by) VALUES(?,?,?,?)";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setString(2, body);
            ps.setString(3, linkUrl);
            ps.setLong(4, createdBy);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            throw new DaoException("No generated key for notifications", null);
        } catch (SQLException e) {
            throw new DaoException("createNotification failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Notification> listLatest(int limit) {
        String sql = "SELECT TOP (?) notification_id,title,body,link_url,created_by,created_at FROM notifications ORDER BY created_at DESC";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                List<Notification> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new Notification(
                            rs.getLong("notification_id"),
                            rs.getString("title"),
                            rs.getString("body"),
                            rs.getString("link_url"),
                            rs.getLong("created_by"),
                            rs.getObject("created_at", LocalDateTime.class)
                    ));
                }
                return out;
            }
        } catch (SQLException e) {
            throw new DaoException("listLatest failed", e);
        }
    }
}
