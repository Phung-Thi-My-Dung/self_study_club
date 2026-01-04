package com.selfstudyclub.dao.jdbc;

import com.selfstudyclub.dao.DaoException;
import com.selfstudyclub.dao.TaskCompletionDao;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class JdbcTaskCompletionDao implements TaskCompletionDao {
    private final DataSource ds;
    public JdbcTaskCompletionDao(DataSource ds) { this.ds = ds; }

    @Override
    public long createCompletion(long accountId, long taskId, LocalDate completedDate, String note) {
        String sql = "INSERT INTO task_completions(account_id,task_id,completed_date,note) VALUES(?,?,?,?)";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, accountId);
            ps.setLong(2, taskId);
            ps.setObject(3, completedDate);
            ps.setString(4, note);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            throw new DaoException("No generated key for task_completions", null);
        } catch (SQLException e) {
            throw new DaoException("createCompletion failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Set<LocalDate> activeDaysInMonth(long accountId, long courseId, int year, int month) {
        String sql =
            "SELECT DISTINCT tc.completed_date " +
            "FROM task_completions tc JOIN tasks t ON t.task_id=tc.task_id " +
            "WHERE tc.account_id=? AND t.course_id=? AND YEAR(tc.completed_date)=? AND MONTH(tc.completed_date)=?";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            ps.setLong(2, courseId);
            ps.setInt(3, year);
            ps.setInt(4, month);
            try (ResultSet rs = ps.executeQuery()) {
                Set<LocalDate> out = new HashSet<>();
                while (rs.next()) out.add(rs.getObject("completed_date", LocalDate.class));
                return out;
            }
        } catch (SQLException e) {
            throw new DaoException("activeDaysInMonth failed", e);
        }
    }
}
