package com.selfstudyclub.dao.jdbc;

import com.selfstudyclub.dao.DaoException;
import com.selfstudyclub.dao.TaskDao;
import com.selfstudyclub.model.Task;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcTaskDao implements TaskDao {
    private final DataSource ds;
    public JdbcTaskDao(DataSource ds) { this.ds = ds; }

    @Override
    public long createTask(long courseId, String title, String description, long createdBy) {
        String sql = "INSERT INTO tasks(course_id,title,description,created_by) VALUES(?,?,?,?)";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, courseId);
            ps.setString(2, title);
            ps.setString(3, description);
            ps.setLong(4, createdBy);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            throw new DaoException("No generated key for tasks", null);
        } catch (SQLException e) {
            throw new DaoException("createTask failed", e);
        }
    }

    @Override
    public List<Task> listByCourse(long courseId, int page, int pageSize) {
        int offset = Math.max(0, (page - 1) * pageSize);
        String sql = "SELECT task_id,course_id,title,description,created_by,is_deleted,created_at FROM tasks " +
                "WHERE is_deleted=0 AND course_id=? ORDER BY task_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, courseId);
            ps.setInt(2, offset);
            ps.setInt(3, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                List<Task> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new Task(
                            rs.getLong("task_id"),
                            rs.getLong("course_id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getLong("created_by"),
                            rs.getBoolean("is_deleted"),
                            rs.getObject("created_at", LocalDateTime.class)
                    ));
                }
                return out;
            }
        } catch (SQLException e) {
            throw new DaoException("listByCourse failed", e);
        }
    }

    @Override
    public long countByCourse(long courseId) {
        String sql = "SELECT COUNT(*) AS c FROM tasks WHERE is_deleted=0 AND course_id=?";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, courseId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getLong("c"); }
        } catch (SQLException e) {
            throw new DaoException("countByCourse failed", e);
        }
    }
}
