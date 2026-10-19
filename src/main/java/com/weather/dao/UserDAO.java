package com.weather.dao;

import com.weather.config.DatabaseConnection;
import com.weather.model.User;

import java.sql.*;

public class UserDAO {

    public User findById(int userId) throws SQLException {
        String sql = "SELECT user_id, full_name, mobile, email, password_hash, password_salt, city_id, is_active FROM Users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setUserId(rs.getInt("user_id"));
                    u.setFullName(rs.getString("full_name"));
                    u.setMobile(rs.getString("mobile"));
                    u.setEmail(rs.getString("email"));
                    u.setPasswordHash(rs.getString("password_hash"));
                    u.setPasswordSalt(rs.getString("password_salt"));
                    int cId = rs.getInt("city_id");
                    if (!rs.wasNull()) u.setCityId(cId);
                    u.setActive(rs.getInt("is_active") == 1);
                    return u;
                }
            }
        }
        return null;
    }

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT user_id, full_name, mobile, email, password_hash, password_salt, city_id, is_active FROM Users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setUserId(rs.getInt("user_id"));
                    u.setFullName(rs.getString("full_name"));
                    u.setMobile(rs.getString("mobile"));
                    u.setEmail(rs.getString("email"));
                    u.setPasswordHash(rs.getString("password_hash"));
                    u.setPasswordSalt(rs.getString("password_salt"));
                    int cId = rs.getInt("city_id");
                    if (!rs.wasNull()) u.setCityId(cId);
                    u.setActive(rs.getInt("is_active") == 1);
                    return u;
                }
            }
        }
        return null;
    }

    public boolean updateCity(int userId, int cityId) throws SQLException {
        String sql = "UPDATE Users SET city_id = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cityId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }
}
