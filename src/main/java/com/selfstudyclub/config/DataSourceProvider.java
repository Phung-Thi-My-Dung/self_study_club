package com.selfstudyclub.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * HikariCP pooled datasource.
 */
public final class DataSourceProvider {
    private final HikariDataSource ds;

    public DataSourceProvider(AppConfig cfg) {
        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(cfg.dbUrl());
        hc.setUsername(cfg.dbUser());
        hc.setPassword(cfg.dbPassword());
        hc.setMaximumPoolSize(cfg.dbPoolSize());
        hc.setPoolName("SelfStudyClubPool");
        this.ds = new HikariDataSource(hc);
    }

    public DataSource dataSource() {
        return ds;
    }
}
