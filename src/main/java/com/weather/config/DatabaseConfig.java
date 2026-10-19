package com.weather.config;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database configuration helper providing connection management
 * and datasource parameters for Microsoft Access MDB and SQLite engines.
 */
public final class DatabaseConfig {

    private DatabaseConfig() {}

    public static Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public static String getDatabaseType() {
        java.io.File sqlite = new java.io.File("ForecastDBMaster.db");
        if (sqlite.exists()) {
            return "SQLite";
        }
        return "Microsoft Access (.mdb)";
    }
}
