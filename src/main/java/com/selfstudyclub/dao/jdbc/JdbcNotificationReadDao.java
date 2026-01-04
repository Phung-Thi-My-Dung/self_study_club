package com.selfstudyclub.dao.jdbc;

import com.selfstudyclub.dao.DaoException;
import com.selfstudyclub.dao.NotificationReadDao;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class JdbcNotificationReadDao implements NotificationReadDao {
    private final DataSource ds;
    public JdbcNotificationReadDao(DataSource ds) { this.ds = ds; }

    @Override
    public void markRead(long notificationId, long accountId) {
        String sql = "IF NOT EXISTS (SELECT 1 FROM notification_reads WHERE notification_id=? AND account_id=?) " +
                "INSERT INTO notification_reads(notification_id,account_id) VALUES(?,?)";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, notificationId);
            ps.setLong(2, accountId);
            ps.setLong(3, notificationId);
            ps.setLong(4, accountId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("markRead failed", e);
        }
    }

    @Override
    public Set<Long> readNotificationIds(long accountId) {
        String sql = "SELECT notification_id FROM notification_reads WHERE account_id=?";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                Set<Long> out = new HashSet<>();
                while (rs.next()) out.add(rs.getLong("notification_id"));
                return out;
            }
        } catch (SQLException e) {
            throw new DaoException("readNotificationIds failed", e);
        }
    }
}
