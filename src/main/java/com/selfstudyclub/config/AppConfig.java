package com.selfstudyclub.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads application.properties (keep config out of code).
 */
public final class AppConfig {
    private final Properties props;

    private AppConfig(Properties props) {
        this.props = props;
    }

    public static AppConfig load() {
        Properties p = new Properties();
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in == null) throw new IllegalStateException("Missing application.properties");
            p.load(in);
            return new AppConfig(p);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config", e);
        }
    }

    public String appName() { return props.getProperty("app.name", "Self-Study Club"); }
    public int pageSize() { return Integer.parseInt(props.getProperty("app.pageSize", "50")); }

    public String dbUrl() { return require("db.url"); }
    public String dbUser() { return require("db.user"); }
    public String dbPassword() { return require("db.password"); }
    public int dbPoolSize() { return Integer.parseInt(props.getProperty("db.poolSize", "8")); }

    private String require(String key) {
        String v = props.getProperty(key);
        if (v == null || v.isBlank()) throw new IllegalStateException("Missing config: " + key);
        return v.trim();
    }
}
