package com.weather.dao;

import com.weather.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserCityDAO {

    public boolean associate(int userId, int cityId) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM UserCities WHERE user_id = ? AND city_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, userId);
            checkPs.setInt(2, cityId);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        }
        String sql = "INSERT INTO UserCities (user_id, city_id, is_favorite, display_order) VALUES (?, ?, 0, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, cityId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean setFavorite(int userId, int cityId, boolean isFavorite) throws SQLException {
        String sql = "UPDATE UserCities SET is_favorite = ? WHERE user_id = ? AND city_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, isFavorite ? 1 : 0);
            ps.setInt(2, userId);
            ps.setInt(3, cityId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Integer> getFavoriteCityIds(int userId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT city_id FROM UserCities WHERE user_id = ? AND is_favorite = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("city_id"));
                }
            }
        }
        return ids;
    }
}
