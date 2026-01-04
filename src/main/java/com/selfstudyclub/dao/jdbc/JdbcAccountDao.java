package com.selfstudyclub.dao.jdbc;

import com.selfstudyclub.dao.AccountDao;
import com.selfstudyclub.dao.DaoException;
import com.selfstudyclub.model.Account;
import com.selfstudyclub.security.AccountStatus;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class JdbcAccountDao implements AccountDao {
    private final DataSource ds;
    public JdbcAccountDao(DataSource ds) { this.ds = ds; }

    @Override
    public Optional<Account> findByEmail(String email) {
        String sql = "SELECT account_id,email,password_hash,status,created_at,updated_at FROM accounts WHERE email=?";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("findByEmail failed", e);
        }
    }

    @Override
    public Optional<Account> findById(long accountId) {
        String sql = "SELECT account_id,email,password_hash,status,created_at,updated_at FROM accounts WHERE account_id=?";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("findById failed", e);
        }
    }

    @Override
    public long createAccount(String email, String passwordHash) {
        String sql = "INSERT INTO accounts(email,password_hash) VALUES(?,?)";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, email);
            ps.setString(2, passwordHash);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            throw new DaoException("No generated key for accounts", null);
        } catch (SQLException e) {
            throw new DaoException("createAccount failed", e);
        }
    }

    @Override
    public void updateStatus(long accountId, String status) {
        String sql = "UPDATE accounts SET status=?, updated_at=SYSDATETIME() WHERE account_id=?";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, accountId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("updateStatus failed", e);
        }
    }

    private Account map(ResultSet rs) throws SQLException {
        return new Account(
                rs.getLong("account_id"),
                rs.getString("email"),
                rs.getString("password_hash"),
                AccountStatus.fromDb(rs.getString("status")),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getObject("updated_at", LocalDateTime.class)
        );
    }
}
