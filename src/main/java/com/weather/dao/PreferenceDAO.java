package com.weather.dao;

import com.weather.config.DatabaseConnection;
import com.weather.model.UserPreferences;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PreferenceDAO {

    public UserPreferences findByUserId(int userId) throws SQLException {
        String sql = "SELECT preference_id, user_id, temperature_unit, theme, default_city_id, " +
                     "rain_alert_threshold, notifications_enabled FROM UserPreferences WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserPreferences p = new UserPreferences(
                        rs.getInt("user_id"),
                        rs.getString("temperature_unit"),
                        rs.getString("theme"),
                        rs.getInt("default_city_id"),
                        rs.getInt("rain_alert_threshold"),
                        rs.getInt("notifications_enabled") == 1
                    );
                    p.setPreferenceId(rs.getInt("preference_id"));
                    return p;
                }
            }
        }
        return new UserPreferences(userId, "C", "light", 1, 70, true);
    }

    public boolean saveOrUpdate(UserPreferences prefs) throws SQLException {
        String sql = "INSERT INTO UserPreferences (user_id, temperature_unit, theme, default_city_id, " +
                     "rain_alert_threshold, notifications_enabled) VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT(user_id) DO UPDATE SET " +
                     "temperature_unit = excluded.temperature_unit, " +
                     "theme = excluded.theme, " +
                     "default_city_id = excluded.default_city_id, " +
                     "rain_alert_threshold = excluded.rain_alert_threshold, " +
                     "notifications_enabled = excluded.notifications_enabled";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, prefs.getUserId());
            ps.setString(2, prefs.getTemperatureUnit());
            ps.setString(3, prefs.getTheme());
            ps.setInt(4, prefs.getDefaultCityId());
            ps.setInt(5, prefs.getRainAlertThreshold());
            ps.setInt(6, prefs.isNotificationsEnabled() ? 1 : 0);
            return ps.executeUpdate() > 0;
        }
    }
}
