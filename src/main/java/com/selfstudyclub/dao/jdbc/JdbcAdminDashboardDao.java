package com.selfstudyclub.dao.jdbc;

import com.selfstudyclub.dao.AdminDashboardDao;
import com.selfstudyclub.dao.DaoException;
import com.selfstudyclub.model.DashboardStats;
import com.selfstudyclub.model.MemberRow;
import com.selfstudyclub.model.TopCourseRow;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcAdminDashboardDao implements AdminDashboardDao {
    private final DataSource ds;
    public JdbcAdminDashboardDao(DataSource ds) { this.ds = ds; }

    @Override
    public DashboardStats loadStats() {
        String sql =
            "SELECT " +
            " (SELECT COUNT(*) FROM accounts a " +
            "  JOIN user_roles ur ON ur.account_id=a.account_id " +
            "  JOIN roles r ON r.role_id=ur.role_id " +
            "  WHERE a.status <> 'DELETED' AND r.role_code='MEMBER') AS total_members, " +
            " (SELECT COUNT(DISTINCT account_id) FROM task_completions WHERE completed_date=CAST(GETDATE() AS DATE)) AS members_today, " +
            " (SELECT COUNT(*) FROM courses WHERE is_deleted=0) AS total_courses";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return new DashboardStats(rs.getLong("total_members"), rs.getLong("members_today"), rs.getLong("total_courses"));
        } catch (SQLException e) {
            throw new DaoException("loadStats failed", e);
        }
    }

    @Override
    public List<TopCourseRow> top5CoursesToday() {
        String sql =
            "SELECT TOP 5 t.course_id, c.title, COUNT(*) AS completions_today " +
            "FROM task_completions tc " +
            "JOIN tasks t ON t.task_id=tc.task_id " +
            "JOIN courses c ON c.course_id=t.course_id " +
            "WHERE tc.completed_date=CAST(GETDATE() AS DATE) AND c.is_deleted=0 " +
            "GROUP BY t.course_id, c.title ORDER BY completions_today DESC";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<TopCourseRow> out = new ArrayList<>();
            while (rs.next()) out.add(new TopCourseRow(rs.getLong("course_id"), rs.getString("title"), rs.getLong("completions_today")));
            return out;
        } catch (SQLException e) {
            throw new DaoException("top5CoursesToday failed", e);
        }
    }

    @Override
    public List<MemberRow> membersActiveToday(int page, int pageSize) {
        int offset = Math.max(0, (page - 1) * pageSize);
        String sql =
            "SELECT a.account_id, a.email, up.full_name " +
            "FROM ( " +
            "  SELECT DISTINCT account_id FROM task_completions " +
            "  WHERE completed_date=CAST(GETDATE() AS DATE) " +
            "  ORDER BY account_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY " +
            ") x " +
            "JOIN accounts a ON a.account_id=x.account_id " +
            "LEFT JOIN user_profiles up ON up.account_id=a.account_id " +
            "ORDER BY a.account_id DESC";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, offset);
            ps.setInt(2, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                List<MemberRow> out = new ArrayList<>();
                while (rs.next()) out.add(new MemberRow(rs.getLong("account_id"), rs.getString("email"), rs.getString("full_name")));
                return out;
            }
        } catch (SQLException e) {
            throw new DaoException("membersActiveToday failed", e);
        }
    }
}
