package com.selfstudyclub.dao.jdbc;

import com.selfstudyclub.dao.DaoException;
import com.selfstudyclub.dao.UserRoleDao;
import com.selfstudyclub.security.RoleCode;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class JdbcUserRoleDao implements UserRoleDao {
    private final DataSource ds;
    public JdbcUserRoleDao(DataSource ds) { this.ds = ds; }

    @Override
    public void assignRole(long accountId, int roleId) {
        String sql = "INSERT INTO user_roles(account_id,role_id) VALUES(?,?)";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            ps.setInt(2, roleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("assignRole failed", e);
        }
    }

    @Override
    public Set<RoleCode> getRoles(long accountId) {
        String sql = "SELECT r.role_code FROM user_roles ur JOIN roles r ON r.role_id=ur.role_id WHERE ur.account_id=?";
        try (Connection c = JdbcUtils.getConnection(ds);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                Set<RoleCode> out = new HashSet<>();
                while (rs.next()) out.add(RoleCode.fromDb(rs.getString("role_code")));
                return out;
            }
        } catch (SQLException e) {
            throw new DaoException("getRoles failed", e);
        }
    }
}
