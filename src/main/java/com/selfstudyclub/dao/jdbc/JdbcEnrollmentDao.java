package com.selfstudyclub.dao.jdbc;

import com.selfstudyclub.dao.DaoException;
import com.selfstudyclub.dao.EnrollmentDao;

import javax.sql.DataSource;
import java.sql.*;

public class JdbcEnrollmentDao implements EnrollmentDao {
    private final DataSource ds;
    public JdbcEnrollmentDao(DataSource ds) { this.ds = ds; }

    @Override
    public void enrollActive(long accountId, long courseId) {
        String sql = "INSERT INTO enrollments(account_id,course_id,status) VALUES(?,?,'ACTIVE')";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            ps.setLong(2, courseId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("enrollActive failed", e);
        }
    }

    @Override
    public boolean isActiveEnrolled(long accountId, long courseId) {
        String sql = "SELECT 1 FROM enrollments WHERE account_id=? AND course_id=? AND status='ACTIVE'";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            ps.setLong(2, courseId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new DaoException("isActiveEnrolled failed", e);
        }
    }
}
