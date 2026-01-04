package com.selfstudyclub.dao.jdbc;

import com.selfstudyclub.dao.DaoException;
import com.selfstudyclub.dao.UserProfileDao;
import com.selfstudyclub.model.UserProfile;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class JdbcUserProfileDao implements UserProfileDao {
    private final DataSource ds;
    public JdbcUserProfileDao(DataSource ds) { this.ds = ds; }

    @Override
    public Optional<UserProfile> findByAccountId(long accountId) {
        String sql = "SELECT account_id,full_name,date_of_birth,gender,phone,address,bio,joined_at,updated_at " +
                "FROM user_profiles WHERE account_id=?";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("findByAccountId failed", e);
        }
    }

    @Override
    public void upsert(UserProfile p) {
        String existsSql = "SELECT 1 FROM user_profiles WHERE account_id=?";
        String insertSql = "INSERT INTO user_profiles(account_id,full_name,date_of_birth,gender,phone,address,bio,joined_at) " +
                "VALUES(?,?,?,?,?,?,?,?)";
        String updateSql = "UPDATE user_profiles SET full_name=?,date_of_birth=?,gender=?,phone=?,address=?,bio=?,joined_at=?,updated_at=SYSDATETIME() " +
                "WHERE account_id=?";
        try (Connection c = JdbcUtils.getConnection(ds)) {
            boolean exists;
            try (PreparedStatement ps = c.prepareStatement(existsSql)) {
                ps.setLong(1, p.accountId());
                try (ResultSet rs = ps.executeQuery()) { exists = rs.next(); }
            }

            if (!exists) {
                try (PreparedStatement ps = c.prepareStatement(insertSql)) {
                    ps.setLong(1, p.accountId());
                    ps.setString(2, p.fullName());
                    ps.setObject(3, p.dateOfBirth());
                    ps.setString(4, p.gender());
                    ps.setString(5, p.phone());
                    ps.setString(6, p.address());
                    ps.setString(7, p.bio());
                    ps.setObject(8, p.joinedAt());
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement(updateSql)) {
                    ps.setString(1, p.fullName());
                    ps.setObject(2, p.dateOfBirth());
                    ps.setString(3, p.gender());
                    ps.setString(4, p.phone());
                    ps.setString(5, p.address());
                    ps.setString(6, p.bio());
                    ps.setObject(7, p.joinedAt());
                    ps.setLong(8, p.accountId());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DaoException("upsert profile failed", e);
        }
    }

    private UserProfile map(ResultSet rs) throws SQLException {
        return new UserProfile(
                rs.getLong("account_id"),
                rs.getString("full_name"),
                rs.getObject("date_of_birth", LocalDate.class),
                rs.getString("gender"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("bio"),
                rs.getObject("joined_at", LocalDate.class),
                rs.getObject("updated_at", LocalDateTime.class)
        );
    }
}
