package com.selfstudyclub.dao.jdbc;

import com.selfstudyclub.dao.CourseDao;
import com.selfstudyclub.dao.DaoException;
import com.selfstudyclub.model.Course;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcCourseDao implements CourseDao {
    private final DataSource ds;
    public JdbcCourseDao(DataSource ds) { this.ds = ds; }

    @Override
    public long createCourse(String title, String description, long createdBy) {
        String sql = "INSERT INTO courses(title,description,created_by) VALUES(?,?,?)";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setLong(3, createdBy);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            throw new DaoException("No generated key for courses", null);
        } catch (SQLException e) {
            throw new DaoException("createCourse failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void softDeleteCourse(long courseId, long requesterId) {
        String sql = "UPDATE courses SET is_deleted=1, updated_at=SYSDATETIME() WHERE course_id=? AND created_by=?";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, courseId);
            ps.setLong(2, requesterId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("softDeleteCourse failed", e);
        }
    }

    @Override
    public List<Course> listAll(int page, int pageSize) {
        String sql = "SELECT course_id,title,description,created_by,is_deleted,created_at FROM courses " +
                "WHERE is_deleted=0 ORDER BY course_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        return list(sql, page, pageSize, 0L);
    }

    @Override
    public List<Course> listByCreator(long createdBy, int page, int pageSize) {
        String sql = "SELECT course_id,title,description,created_by,is_deleted,created_at FROM courses " +
                "WHERE is_deleted=0 AND created_by=? ORDER BY course_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        return list(sql, page, pageSize, createdBy);
    }

    @Override
    public long countAll() {
        String sql = "SELECT COUNT(*) AS c FROM courses WHERE is_deleted=0";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next(); return rs.getLong("c");
        } catch (SQLException e) {
            throw new DaoException("countAll failed", e);
        }
    }

    @Override
    public long countByCreator(long createdBy) {
        String sql = "SELECT COUNT(*) AS c FROM courses WHERE is_deleted=0 AND created_by=?";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, createdBy);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next(); return rs.getLong("c");
            }
        } catch (SQLException e) {
            throw new DaoException("countByCreator failed", e);
        }
    }

    private List<Course> list(String sql, int page, int pageSize, long createdBy) {
        int offset = Math.max(0, (page - 1) * pageSize);
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql)) {
            int idx = 1;
            if (createdBy != 0L) ps.setLong(idx++, createdBy);
            ps.setInt(idx++, offset);
            ps.setInt(idx, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                List<Course> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new Course(
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
            throw new DaoException("list courses failed", e);
        }
    }
}
