-- ForecastDBMaster_schema.sql
-- Comprehensive Schema for Weather Dashboard (HTML5 + Bootstrap 5 + Java + Jetty 12)
-- Compatible with SQLite and DB Browser for SQLite

PRAGMA foreign_keys = ON;

-- Table A: Cities - Stores unique city/location records selected through Google Maps
CREATE TABLE IF NOT EXISTS Cities (
    city_id INTEGER PRIMARY KEY AUTOINCREMENT,
    city_name TEXT NOT NULL,
    country TEXT NOT NULL,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    timezone TEXT DEFAULT 'Asia/Kolkata',
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table B: Users - Stores registration and profile information
CREATE TABLE IF NOT EXISTS Users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name TEXT NOT NULL,
    mobile TEXT,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    password_salt TEXT,
    city_id INTEGER,
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (city_id) REFERENCES Cities(city_id) ON DELETE SET NULL
);

-- Table C: UserCities - Junction table supporting multiple favorite cities per user
CREATE TABLE IF NOT EXISTS UserCities (
    user_city_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    city_id INTEGER NOT NULL,
    is_favorite INTEGER NOT NULL DEFAULT 0,
    display_order INTEGER NOT NULL DEFAULT 0,
    added_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, city_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (city_id) REFERENCES Cities(city_id) ON DELETE CASCADE
);

-- Table D: UserPreferences - Stores user visual and operational preferences
CREATE TABLE IF NOT EXISTS UserPreferences (
    preference_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL UNIQUE,
    temperature_unit TEXT NOT NULL DEFAULT 'C',
    theme TEXT NOT NULL DEFAULT 'light',
    default_city_id INTEGER,
    rain_alert_threshold INTEGER NOT NULL DEFAULT 70,
    notifications_enabled INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (default_city_id) REFERENCES Cities(city_id) ON DELETE SET NULL
);

-- Table E: WeatherAlerts - Stores provider weather warnings and alert history
CREATE TABLE IF NOT EXISTS WeatherAlerts (
    alert_id INTEGER PRIMARY KEY AUTOINCREMENT,
    city_id INTEGER NOT NULL,
    provider_alert_id TEXT,
    event_type TEXT NOT NULL,
    severity TEXT NOT NULL,
    description TEXT,
    start_time DATETIME,
    end_time DATETIME,
    retrieved_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fingerprint TEXT UNIQUE,
    is_active INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (city_id) REFERENCES Cities(city_id) ON DELETE CASCADE
);

-- Table F: WeatherHistory - Stores persistent observation and weather snapshots
CREATE TABLE IF NOT EXISTS WeatherHistory (
    record_id INTEGER PRIMARY KEY AUTOINCREMENT,
    city_id INTEGER NOT NULL,
    recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    temperature REAL NOT NULL,
    humidity INTEGER,
    wind_speed REAL,
    rainfall_mm REAL DEFAULT 0.0,
    description TEXT,
    FOREIGN KEY (city_id) REFERENCES Cities(city_id) ON DELETE CASCADE
);

-- Backward-compatibility table: Forecasts
CREATE TABLE IF NOT EXISTS Forecasts (
    forecast_id INTEGER PRIMARY KEY AUTOINCREMENT,
    city_id INTEGER NOT NULL,
    temperature REAL,
    humidity INTEGER,
    description TEXT,
    wind_speed REAL,
    recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (city_id) REFERENCES Cities(city_id) ON DELETE CASCADE
);

-- Indices for rapid lookups and query efficiency
CREATE INDEX IF NOT EXISTS idx_cities_name ON Cities(city_name);
CREATE INDEX IF NOT EXISTS idx_usercities_user ON UserCities(user_id);
CREATE INDEX IF NOT EXISTS idx_weatheralerts_city ON WeatherAlerts(city_id);
CREATE INDEX IF NOT EXISTS idx_weatheralerts_active ON WeatherAlerts(is_active);
CREATE INDEX IF NOT EXISTS idx_weatherhistory_city ON WeatherHistory(city_id, recorded_at DESC);
CREATE INDEX IF NOT EXISTS idx_forecasts_city ON Forecasts(city_id);

-- Sample Indian Cities
INSERT OR IGNORE INTO Cities (city_name, country, latitude, longitude, timezone) VALUES
    ('Pune', 'India', 18.5204, 73.8567, 'Asia/Kolkata'),
    ('Mumbai', 'India', 19.0760, 72.8777, 'Asia/Kolkata'),
    ('Aurangabad', 'India', 19.8762, 75.3433, 'Asia/Kolkata'),
    ('Jalna', 'India', 19.8347, 75.8816, 'Asia/Kolkata'),
    ('Delhi', 'India', 28.6139, 77.2090, 'Asia/Kolkata'),
    ('Bengaluru', 'India', 12.9716, 77.5946, 'Asia/Kolkata'),
    ('Hyderabad', 'India', 17.3850, 78.4867, 'Asia/Kolkata'),
    ('Chennai', 'India', 13.0827, 80.2707, 'Asia/Kolkata'),
    ('Kolkata', 'India', 22.5726, 88.3639, 'Asia/Kolkata');
