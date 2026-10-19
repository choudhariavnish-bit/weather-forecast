package com.weatherapp.servlet;

import com.weatherapp.db.Database;
import com.weatherapp.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/preferences")
public class PreferencesServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            HttpSession s = requireSession(req, res);
            int userId = (int) s.getAttribute("UserID");

            Map<String, Object> prefs = new HashMap<>();
            prefs.put("temperatureUnit", "C");
            prefs.put("theme", "light");
            prefs.put("defaultCityId", s.getAttribute("UserCityID"));
            prefs.put("rainAlertThreshold", 70);
            prefs.put("notificationsEnabled", true);

            try (Connection c = Database.getConnection();
                 PreparedStatement p = c.prepareStatement(
                     "SELECT temperature_unit, theme, default_city_id, rain_alert_threshold, notifications_enabled " +
                     "FROM UserPreferences WHERE user_id = ?")) {
                p.setInt(1, userId);
                try (ResultSet r = p.executeQuery()) {
                    if (r.next()) {
                        prefs.put("temperatureUnit", r.getString("temperature_unit"));
                        prefs.put("theme", r.getString("theme"));
                        int dCity = r.getInt("default_city_id");
                        if (dCity > 0) prefs.put("defaultCityId", dCity);
                        prefs.put("rainAlertThreshold", r.getInt("rain_alert_threshold"));
                        prefs.put("notificationsEnabled", r.getInt("notifications_enabled") == 1);
                    }
                }
            }
            Json.write(res, prefs);
        } catch (Exception e) {
            res.setStatus(401);
            Json.write(res, Map.of("message", e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        handleSave(req, res);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        handleSave(req, res);
    }

    private void handleSave(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            HttpSession s = requireSession(req, res);
            int userId = (int) s.getAttribute("UserID");

            Map<String, Object> b = Json.M.readValue(Json.body(req), Map.class);
            String unit = String.valueOf(b.getOrDefault("temperatureUnit", "C")).trim().toUpperCase();
            if (!unit.equals("F")) unit = "C";

            String theme = String.valueOf(b.getOrDefault("theme", "light")).trim().toLowerCase();
            if (!theme.equals("dark")) theme = "light";

            int defaultCityId = Integer.parseInt(String.valueOf(b.getOrDefault("defaultCityId", s.getAttribute("UserCityID") != null ? s.getAttribute("UserCityID") : "1")));
            int rainThreshold = Integer.parseInt(String.valueOf(b.getOrDefault("rainAlertThreshold", "70")));
            if (rainThreshold < 0) rainThreshold = 0;
            if (rainThreshold > 100) rainThreshold = 100;

            boolean notifs = Boolean.parseBoolean(String.valueOf(b.getOrDefault("notificationsEnabled", "true")));

            try (Connection c = Database.getConnection();
                 PreparedStatement p = c.prepareStatement(
                     "INSERT INTO UserPreferences (user_id, temperature_unit, theme, default_city_id, rain_alert_threshold, notifications_enabled) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT(user_id) DO UPDATE SET " +
                     "temperature_unit = excluded.temperature_unit, " +
                     "theme = excluded.theme, " +
                     "default_city_id = excluded.default_city_id, " +
                     "rain_alert_threshold = excluded.rain_alert_threshold, " +
                     "notifications_enabled = excluded.notifications_enabled")) {
                p.setInt(1, userId);
                p.setString(2, unit);
                p.setString(3, theme);
                p.setInt(4, defaultCityId);
                p.setInt(5, rainThreshold);
                p.setInt(6, notifs ? 1 : 0);
                p.executeUpdate();
            }

            s.setAttribute("UserCityID", defaultCityId);

            Json.write(res, Map.of(
                "ok", true,
                "temperatureUnit", unit,
                "theme", theme,
                "defaultCityId", defaultCityId,
                "rainAlertThreshold", rainThreshold,
                "notificationsEnabled", notifs
            ));
        } catch (Exception e) {
            res.setStatus(400);
            Json.write(res, Map.of("ok", false, "message", e.getMessage()));
        }
    }
}
