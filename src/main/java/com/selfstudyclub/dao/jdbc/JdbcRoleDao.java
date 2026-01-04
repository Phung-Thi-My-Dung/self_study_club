package com.selfstudyclub.dao.jdbc;

import com.selfstudyclub.dao.DaoException;
import com.selfstudyclub.dao.RoleDao;
import com.selfstudyclub.security.RoleCode;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class JdbcRoleDao implements RoleDao {
    private final DataSource ds;
    public JdbcRoleDao(DataSource ds) { this.ds = ds; }

    @Override
    public Optional<Integer> findRoleId(RoleCode roleCode) {
        String sql = "SELECT role_id FROM roles WHERE role_code=?";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, roleCode.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(rs.getInt("role_id"));
            }
        } catch (SQLException e) {
            throw new DaoException("findRoleId failed", e);
        }
    }
}
