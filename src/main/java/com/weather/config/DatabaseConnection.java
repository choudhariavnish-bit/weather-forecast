package com.weather.config;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    public static Connection getConnection() throws SQLException {
        // Check for SQLite database first as active runtime engine
        File sqliteDb = new File("ForecastDBMaster.db");
        if (!sqliteDb.exists()) {
            sqliteDb = new File("src/main/webapp/ForecastDBMaster.db");
        }

        if (sqliteDb.exists()) {
            try {
                Class.forName("org.sqlite.JDBC");
                Connection c = DriverManager.getConnection("jdbc:sqlite:" + sqliteDb.getAbsolutePath());
                c.createStatement().execute("PRAGMA foreign_keys = ON;");
                return c;
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite Driver not found", e);
            }
        }

        // Fallback to Access MDB file if present
        File mdbFile = new File("ForecastDBMaster.mdb");
        if (!mdbFile.exists()) {
            mdbFile = new File("database/ForecastDBMaster.mdb");
        }
        if (!mdbFile.exists()) {
            mdbFile = new File("src/main/webapp/ForecastDBMaster.mdb");
        }

        if (mdbFile.exists()) {
            try {
                Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
                return DriverManager.getConnection("jdbc:ucanaccess://" + mdbFile.getAbsolutePath() + ";memory=false");
            } catch (ClassNotFoundException e) {
                throw new SQLException("UCanAccess Driver not found", e);
            }
        }

        // Default SQLite connection
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:ForecastDBMaster.db");
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }
    }
}
