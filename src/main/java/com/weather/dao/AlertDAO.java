package com.weather.dao;

import com.weather.config.DatabaseConnection;
import com.weather.model.WeatherAlert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlertDAO {

    public List<WeatherAlert> findActiveAlerts(int cityId) throws SQLException {
        List<WeatherAlert> list = new ArrayList<>();
        String sql = "SELECT a.alert_id, a.city_id, c.city_name, a.provider_alert_id, a.event_type, " +
                     "a.severity, a.description, a.start_time, a.end_time, a.retrieved_at, a.is_active " +
                     "FROM WeatherAlerts a JOIN Cities c ON a.city_id = c.city_id " +
                     "WHERE a.is_active = 1 " +
                     (cityId > 0 ? "AND a.city_id = ? " : "") +
                     "ORDER BY a.retrieved_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (cityId > 0) ps.setInt(1, cityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WeatherAlert alert = new WeatherAlert();
                    alert.setAlertId(rs.getInt("alert_id"));
                    alert.setCityId(rs.getInt("city_id"));
                    alert.setCityName(rs.getString("city_name"));
                    alert.setProviderAlertId(rs.getString("provider_alert_id"));
                    alert.setEventType(rs.getString("event_type"));
                    alert.setSeverity(rs.getString("severity"));
                    alert.setDescription(rs.getString("description"));
                    alert.setRetrievedAt(rs.getString("retrieved_at"));
                    alert.setActive(rs.getInt("is_active") == 1);
                    list.add(alert);
                }
            }
        }
        return list;
    }

    public List<WeatherAlert> findAlertHistory(int cityId, int limit) throws SQLException {
        List<WeatherAlert> list = new ArrayList<>();
        String sql = "SELECT a.alert_id, a.city_id, c.city_name, a.provider_alert_id, a.event_type, " +
                     "a.severity, a.description, a.start_time, a.end_time, a.retrieved_at, a.is_active " +
                     "FROM WeatherAlerts a JOIN Cities c ON a.city_id = c.city_id " +
                     (cityId > 0 ? "WHERE a.city_id = ? " : "") +
                     "ORDER BY a.retrieved_at DESC LIMIT " + (limit > 0 ? limit : 50);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (cityId > 0) ps.setInt(1, cityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WeatherAlert alert = new WeatherAlert();
                    alert.setAlertId(rs.getInt("alert_id"));
                    alert.setCityId(rs.getInt("city_id"));
                    alert.setCityName(rs.getString("city_name"));
                    alert.setProviderAlertId(rs.getString("provider_alert_id"));
                    alert.setEventType(rs.getString("event_type"));
                    alert.setSeverity(rs.getString("severity"));
                    alert.setDescription(rs.getString("description"));
                    alert.setRetrievedAt(rs.getString("retrieved_at"));
                    alert.setActive(rs.getInt("is_active") == 1);
                    list.add(alert);
                }
            }
        }
        return list;
    }

    public boolean saveAlert(WeatherAlert alert) {
        String sql = "INSERT OR IGNORE INTO WeatherAlerts (city_id, provider_alert_id, event_type, severity, description, fingerprint, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 1)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, alert.getCityId());
            ps.setString(2, alert.getProviderAlertId());
            ps.setString(3, alert.getEventType());
            ps.setString(4, alert.getSeverity());
            ps.setString(5, alert.getDescription());
            ps.setString(6, alert.getFingerprint());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
