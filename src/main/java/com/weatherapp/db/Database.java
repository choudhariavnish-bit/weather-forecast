package com.weatherapp.db;

import com.weatherapp.config.AppConfig;
import jakarta.servlet.ServletContext;
import java.io.File;
import java.sql.*;

public final class Database {
    private static String dbPath;

    private Database() {}

    public static void main(String[] args) {
        try {
            init(null);
            System.out.println("Database initialized successfully at: " + dbPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void init(ServletContext ctx) throws Exception {
        String fileName = AppConfig.get("DB_FILE");
        if (fileName == null || fileName.isBlank()) {
            fileName = "ForecastDBMaster.db";
        }
        File f = null;
        if (ctx != null && ctx.getRealPath("/") != null) {
            f = new File(ctx.getRealPath("/"), fileName);
        }
        if (f == null || !f.exists()) {
            File fallback = new File(fileName);
            if (fallback.exists()) {
                f = fallback;
            } else {
                f = (ctx != null && ctx.getRealPath("/") != null) ? new File(ctx.getRealPath("/"), fileName) : fallback;
            }
        }
        dbPath = f.getAbsolutePath();
        Class.forName("org.sqlite.JDBC");
        ensureTables();
    }

    public static Connection getConnection() throws SQLException {
        if (dbPath == null) {
            String fileName = AppConfig.get("DB_FILE");
            if (fileName == null || fileName.isBlank()) fileName = "ForecastDBMaster.db";
            File fallback = new File(fileName);
            dbPath = fallback.getAbsolutePath();
        }
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        try (Statement s = conn.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    private static void ensureTables() throws SQLException {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            // Table A: Cities
            s.executeUpdate("CREATE TABLE IF NOT EXISTS Cities (" +
                "city_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "city_name TEXT NOT NULL, " +
                "country TEXT NOT NULL, " +
                "latitude REAL NOT NULL, " +
                "longitude REAL NOT NULL, " +
                "timezone TEXT DEFAULT 'Asia/Kolkata', " +
                "is_active INTEGER NOT NULL DEFAULT 1, " +
                "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)");

            // Table B: Users
            s.executeUpdate("CREATE TABLE IF NOT EXISTS Users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "full_name TEXT NOT NULL, " +
                "mobile TEXT, " +
                "email TEXT NOT NULL UNIQUE, " +
                "password_hash TEXT NOT NULL, " +
                "password_salt TEXT, " +
                "city_id INTEGER, " +
                "is_active INTEGER NOT NULL DEFAULT 1, " +
                "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (city_id) REFERENCES Cities(city_id) ON DELETE SET NULL)");

            // Table C: UserCities
            s.executeUpdate("CREATE TABLE IF NOT EXISTS UserCities (" +
                "user_city_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "city_id INTEGER NOT NULL, " +
                "is_favorite INTEGER NOT NULL DEFAULT 0, " +
                "display_order INTEGER NOT NULL DEFAULT 0, " +
                "added_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE (user_id, city_id), " +
                "FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (city_id) REFERENCES Cities(city_id) ON DELETE CASCADE)");

            // Table D: UserPreferences
            s.executeUpdate("CREATE TABLE IF NOT EXISTS UserPreferences (" +
                "preference_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL UNIQUE, " +
                "temperature_unit TEXT NOT NULL DEFAULT 'C', " +
                "theme TEXT NOT NULL DEFAULT 'light', " +
                "default_city_id INTEGER, " +
                "rain_alert_threshold INTEGER NOT NULL DEFAULT 70, " +
                "notifications_enabled INTEGER NOT NULL DEFAULT 1, " +
                "FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (default_city_id) REFERENCES Cities(city_id) ON DELETE SET NULL)");

            // Table E: WeatherAlerts
            s.executeUpdate("CREATE TABLE IF NOT EXISTS WeatherAlerts (" +
                "alert_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "city_id INTEGER NOT NULL, " +
                "provider_alert_id TEXT, " +
                "event_type TEXT NOT NULL, " +
                "severity TEXT NOT NULL, " +
                "description TEXT, " +
                "start_time DATETIME, " +
                "end_time DATETIME, " +
                "retrieved_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "fingerprint TEXT UNIQUE, " +
                "is_active INTEGER NOT NULL DEFAULT 1, " +
                "FOREIGN KEY (city_id) REFERENCES Cities(city_id) ON DELETE CASCADE)");

            // Table F: WeatherHistory
            s.executeUpdate("CREATE TABLE IF NOT EXISTS WeatherHistory (" +
                "record_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "city_id INTEGER NOT NULL, " +
                "recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "temperature REAL NOT NULL, " +
                "humidity INTEGER, " +
                "wind_speed REAL, " +
                "rainfall_mm REAL DEFAULT 0.0, " +
                "description TEXT, " +
                "FOREIGN KEY (city_id) REFERENCES Cities(city_id) ON DELETE CASCADE)");

            // Forecasts table (backward compatibility)
            s.executeUpdate("CREATE TABLE IF NOT EXISTS Forecasts (" +
                "forecast_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "city_id INTEGER NOT NULL, " +
                "temperature REAL, " +
                "humidity INTEGER, " +
                "description TEXT, " +
                "wind_speed REAL, " +
                "recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (city_id) REFERENCES Cities(city_id) ON DELETE CASCADE)");

            // Migrations for existing tables
            try { s.executeUpdate("ALTER TABLE UserCities ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0"); } catch (Exception ignored) {}
            try { s.executeUpdate("ALTER TABLE UserCities ADD COLUMN display_order INTEGER NOT NULL DEFAULT 0"); } catch (Exception ignored) {}
            try { s.executeUpdate("ALTER TABLE Cities ADD COLUMN timezone TEXT DEFAULT 'Asia/Kolkata'"); } catch (Exception ignored) {}

            // Indices
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_cities_name ON Cities(city_name)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_usercities_user ON UserCities(user_id)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_weatheralerts_city ON WeatherAlerts(city_id)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_weatheralerts_active ON WeatherAlerts(is_active)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_weatherhistory_city ON WeatherHistory(city_id, recorded_at DESC)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_forecasts_city ON Forecasts(city_id)");

            if (count(c, "Cities") == 0) {
                insertCity(c, "Pune", "India", 18.5204, 73.8567);
                insertCity(c, "Mumbai", "India", 19.0760, 72.8777);
                insertCity(c, "Aurangabad", "India", 19.8762, 75.3433);
                insertCity(c, "Jalna", "India", 19.8347, 75.8816);
                insertCity(c, "Delhi", "India", 28.6139, 77.2090);
                insertCity(c, "Bengaluru", "India", 12.9716, 77.5946);
                insertCity(c, "Hyderabad", "India", 17.3850, 78.4867);
                insertCity(c, "Chennai", "India", 13.0827, 80.2707);
                insertCity(c, "Kolkata", "India", 22.5726, 88.3639);
            }
        }
    }

    private static int count(Connection c, String table) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT COUNT(*) FROM " + table)) {
            r.next();
            return r.getInt(1);
        }
    }

    private static void insertCity(Connection c, String name, String country, double lat, double lon) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "INSERT INTO Cities (city_name, country, latitude, longitude, timezone, is_active) VALUES (?, ?, ?, ?, 'Asia/Kolkata', 1)")) {
            p.setString(1, name);
            p.setString(2, country);
            p.setDouble(3, lat);
            p.setDouble(4, lon);
            p.executeUpdate();
        }
    }
}
