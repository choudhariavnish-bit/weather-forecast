package com.weather.dao;

import com.weather.config.DatabaseConnection;
import com.weather.model.City;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CityDAO {

    public List<City> findAll(int userId) throws SQLException {
        List<City> cities = new ArrayList<>();
        String sql = "SELECT c.city_id, c.city_name, c.country, c.latitude, c.longitude, c.timezone, " +
                     "COALESCE(uc.is_favorite, 0) as is_favorite " +
                     "FROM Cities c " +
                     "LEFT JOIN UserCities uc ON c.city_id = uc.city_id AND uc.user_id = ? " +
                     "WHERE c.is_active = 1 " +
                     "ORDER BY is_favorite DESC, c.city_name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    City city = new City(
                        rs.getInt("city_id"),
                        rs.getString("city_name"),
                        rs.getString("country") != null ? rs.getString("country") : "India",
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude")
                    );
                    city.setTimezone(rs.getString("timezone"));
                    city.setFavorite(rs.getInt("is_favorite") == 1);
                    cities.add(city);
                }
            }
        }
        return cities;
    }

    public City findById(int cityId) throws SQLException {
        String sql = "SELECT city_id, city_name, country, latitude, longitude, timezone FROM Cities WHERE city_id = ? AND is_active = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cityId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    City city = new City(
                        rs.getInt("city_id"),
                        rs.getString("city_name"),
                        rs.getString("country") != null ? rs.getString("country") : "India",
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude")
                    );
                    city.setTimezone(rs.getString("timezone"));
                    return city;
                }
            }
        }
        return null;
    }

    public int insert(City city, int userId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if city already exists
            String checkSql = "SELECT city_id FROM Cities WHERE (city_name = ? OR (ABS(latitude - ?) < 0.05 AND ABS(longitude - ?) < 0.05)) AND is_active = 1";
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setString(1, city.getCityName());
                psCheck.setDouble(2, city.getLatitude());
                psCheck.setDouble(3, city.getLongitude());
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        int existingId = rs.getInt("city_id");
                        associateUser(conn, userId, existingId);
                        return existingId;
                    }
                }
            }

            // Insert new city
            String insertSql = "INSERT INTO Cities (city_name, country, latitude, longitude, timezone, is_active) VALUES (?, ?, ?, ?, ?, 1)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, city.getCityName());
                ps.setString(2, city.getCountryCode() != null ? city.getCountryCode() : "India");
                ps.setDouble(3, city.getLatitude());
                ps.setDouble(4, city.getLongitude());
                ps.setString(5, city.getTimezone() != null ? city.getTimezone() : "Asia/Kolkata");
                ps.executeUpdate();
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        int newId = gk.getInt(1);
                        associateUser(conn, userId, newId);
                        return newId;
                    }
                }
            }
        }
        return 0;
    }

    public boolean delete(int cityId, int userId) throws SQLException {
        String sql = "DELETE FROM UserCities WHERE user_id = ? AND city_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, cityId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean toggleFavorite(int cityId, int userId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String checkSql = "SELECT is_favorite FROM UserCities WHERE user_id = ? AND city_id = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, userId);
                psCheck.setInt(2, cityId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        boolean newState = rs.getInt("is_favorite") == 0;
                        String updateSql = "UPDATE UserCities SET is_favorite = ? WHERE user_id = ? AND city_id = ?";
                        try (PreparedStatement psUpd = conn.prepareStatement(updateSql)) {
                            psUpd.setInt(1, newState ? 1 : 0);
                            psUpd.setInt(2, userId);
                            psUpd.setInt(3, cityId);
                            psUpd.executeUpdate();
                            return newState;
                        }
                    } else {
                        String insertSql = "INSERT INTO UserCities (user_id, city_id, is_favorite) VALUES (?, ?, 1)";
                        try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                            psIns.setInt(1, userId);
                            psIns.setInt(2, cityId);
                            psIns.executeUpdate();
                            return true;
                        }
                    }
                }
            }
        }
    }

    private void associateUser(Connection conn, int userId, int cityId) {
        if (userId <= 0 || cityId <= 0) return;
        try {
            try (PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM UserCities WHERE user_id = ? AND city_id = ?")) {
                check.setInt(1, userId);
                check.setInt(2, cityId);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return;
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO UserCities (user_id, city_id, is_favorite, display_order) VALUES (?, ?, 0, 0)")) {
                ps.setInt(1, userId);
                ps.setInt(2, cityId);
                ps.executeUpdate();
            }
        } catch (Exception ignored) {}
    }
}
