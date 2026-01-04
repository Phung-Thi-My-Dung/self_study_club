package com.selfstudyclub.dao.jdbc;

import com.selfstudyclub.dao.DaoException;
import com.selfstudyclub.dao.LeaderboardDao;
import com.selfstudyclub.model.LeaderboardRow;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcLeaderboardDao implements LeaderboardDao {
    private final DataSource ds;
    public JdbcLeaderboardDao(DataSource ds) { this.ds = ds; }

    @Override
    public List<LeaderboardRow> top50ByTotalCompletions() {
        String sql =
            "SELECT TOP 50 a.account_id,a.email,up.full_name,COUNT(*) AS value " +
            "FROM task_completions tc " +
            "JOIN accounts a ON a.account_id=tc.account_id " +
            "LEFT JOIN user_profiles up ON up.account_id=a.account_id " +
            "GROUP BY a.account_id,a.email,up.full_name ORDER BY value DESC";
        return query(sql);
    }

    @Override
    public List<LeaderboardRow> top50ByActiveDays() {
        String sql =
            "SELECT TOP 50 a.account_id,a.email,up.full_name,COUNT(DISTINCT tc.completed_date) AS value " +
            "FROM task_completions tc " +
            "JOIN accounts a ON a.account_id=tc.account_id " +
            "LEFT JOIN user_profiles up ON up.account_id=a.account_id " +
            "GROUP BY a.account_id,a.email,up.full_name ORDER BY value DESC";
        return query(sql);
    }

    private List<LeaderboardRow> query(String sql) {
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<LeaderboardRow> out = new ArrayList<>();
            while (rs.next()) out.add(new LeaderboardRow(rs.getLong("account_id"), rs.getString("email"), rs.getString("full_name"), rs.getLong("value")));
            return out;
        } catch (SQLException e) {
            throw new DaoException("leaderboard query failed", e);
        }
    }
}
