package com.selfstudyclub.dao.jdbc;

import com.selfstudyclub.dao.DaoException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class JdbcUtils {
    private JdbcUtils() {}
    public static Connection getConnection(DataSource ds) {
        try { return ds.getConnection(); }
        catch (SQLException e) { throw new DaoException("Cannot open DB connection", e); }
    }
}
