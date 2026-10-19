package com.weatherapp.servlet;

import com.weatherapp.db.Database;
import com.weatherapp.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/alerts")
public class AlertsServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            String cityParam = req.getParameter("cityId");
            String historyParam = req.getParameter("history");
            boolean history = "true".equalsIgnoreCase(historyParam) || req.getRequestURI().endsWith("/history");

            List<Map<String, Object>> list = new ArrayList<>();
            String sql;
            if (history) {
                sql = "SELECT a.alert_id, a.city_id, c.city_name, c.country, a.provider_alert_id, " +
                      "a.event_type, a.severity, a.description, a.start_time, a.end_time, a.retrieved_at, a.is_active " +
                      "FROM WeatherAlerts a JOIN Cities c ON a.city_id = c.city_id " +
                      (cityParam != null ? "WHERE a.city_id = ? " : "") +
                      "ORDER BY a.retrieved_at DESC LIMIT 50";
            } else {
                sql = "SELECT a.alert_id, a.city_id, c.city_name, c.country, a.provider_alert_id, " +
                      "a.event_type, a.severity, a.description, a.start_time, a.end_time, a.retrieved_at, a.is_active " +
                      "FROM WeatherAlerts a JOIN Cities c ON a.city_id = c.city_id " +
                      "WHERE a.is_active = 1 " +
                      (cityParam != null ? "AND a.city_id = ? " : "") +
                      "ORDER BY a.retrieved_at DESC";
            }

            try (Connection c = Database.getConnection();
                 PreparedStatement p = c.prepareStatement(sql)) {
                if (cityParam != null) {
                    p.setInt(1, Integer.parseInt(cityParam));
                }
                try (ResultSet r = p.executeQuery()) {
                    while (r.next()) {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("id", r.getInt("alert_id"));
                        item.put("cityId", r.getInt("city_id"));
                        item.put("cityName", r.getString("city_name"));
                        item.put("country", r.getString("country"));
                        item.put("eventType", r.getString("event_type") != null ? r.getString("event_type") : "Weather Alert");
                        item.put("severity", r.getString("severity") != null ? r.getString("severity") : "Moderate");
                        item.put("description", r.getString("description") != null ? r.getString("description") : "");
                        item.put("startTime", r.getString("start_time") != null ? r.getString("start_time") : "");
                        item.put("endTime", r.getString("end_time") != null ? r.getString("end_time") : "");
                        item.put("retrievedAt", r.getString("retrieved_at") != null ? r.getString("retrieved_at") : "");
                        item.put("isActive", r.getInt("is_active") == 1);
                        list.add(item);
                    }
                }
            }

            Json.write(res, Map.of(
                "ok", true,
                "history", history,
                "count", list.size(),
                "alerts", list
            ));
        } catch (Exception e) {
            res.setStatus(400);
            Json.write(res, Map.of("ok", false, "message", e.getMessage()));
        }
    }
}
