package com.weatherapp.servlet;

import com.weatherapp.db.Database;
import com.weatherapp.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/api/cities")
public class CitiesServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            HttpSession s = requireSession(req, res);
            int userId = (int) s.getAttribute("UserID");
            List<Map<String, Object>> a = new ArrayList<>();
            String sql = "SELECT c.city_id, c.city_name, c.country, c.latitude, c.longitude, " +
                         "COALESCE(uc.is_favorite, 0) as is_favorite " +
                         "FROM Cities c " +
                         "LEFT JOIN UserCities uc ON c.city_id = uc.city_id AND uc.user_id = ? " +
                         "WHERE c.is_active = 1 " +
                         "ORDER BY is_favorite DESC, c.city_name ASC";
            try (Connection c = Database.getConnection();
                 PreparedStatement p = c.prepareStatement(sql)) {
                p.setInt(1, userId);
                try (ResultSet r = p.executeQuery()) {
                    while (r.next()) {
                        a.add(Map.of(
                            "id", r.getInt("city_id"),
                            "name", r.getString("city_name"),
                            "country", r.getString("country") != null ? r.getString("country") : "IN",
                            "lat", r.getDouble("latitude"),
                            "lon", r.getDouble("longitude"),
                            "isFavorite", r.getInt("is_favorite") == 1
                        ));
                    }
                }
            }
            Json.write(res, a);
        } catch (Exception e) {
            res.setStatus(401);
            Json.write(res, Map.of("message", e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            HttpSession s = requireSession(req, res);
            int userId = (int) s.getAttribute("UserID");
            Map<String, Object> b = Json.M.readValue(Json.body(req), Map.class);

            // Check if action is toggle favorite
            String action = String.valueOf(b.getOrDefault("action", ""));
            if ("toggleFavorite".equalsIgnoreCase(action) || b.containsKey("favoriteCityId")) {
                int targetCityId = Integer.parseInt(String.valueOf(b.getOrDefault("favoriteCityId", b.getOrDefault("cityId", "0"))));
                if (targetCityId <= 0) throw new Exception("Invalid city ID for favorite toggle.");

                boolean newFavState = false;
                try (Connection c = Database.getConnection()) {
                    // Check current state
                    try (PreparedStatement check = c.prepareStatement(
                            "SELECT is_favorite FROM UserCities WHERE user_id = ? AND city_id = ?")) {
                        check.setInt(1, userId);
                        check.setInt(2, targetCityId);
                        try (ResultSet cr = check.executeQuery()) {
                            if (cr.next()) {
                                int curr = cr.getInt("is_favorite");
                                newFavState = (curr == 0);
                                try (PreparedStatement upd = c.prepareStatement(
                                        "UPDATE UserCities SET is_favorite = ? WHERE user_id = ? AND city_id = ?")) {
                                    upd.setInt(1, newFavState ? 1 : 0);
                                    upd.setInt(2, userId);
                                    upd.setInt(3, targetCityId);
                                    upd.executeUpdate();
                                }
                            } else {
                                newFavState = true;
                                try (PreparedStatement ins = c.prepareStatement(
                                        "INSERT INTO UserCities (user_id, city_id, is_favorite) VALUES (?, ?, 1)")) {
                                    ins.setInt(1, userId);
                                    ins.setInt(2, targetCityId);
                                    ins.executeUpdate();
                                }
                            }
                        }
                    }
                }
                Json.write(res, Map.of("ok", true, "cityId", targetCityId, "isFavorite", newFavState));
                return;
            }

            // Normal city addition
            String n = String.valueOf(b.getOrDefault("name", "")).trim();
            String cc = String.valueOf(b.getOrDefault("country", "India")).trim();
            double lat = Double.parseDouble(String.valueOf(b.getOrDefault("lat", "0")));
            double lon = Double.parseDouble(String.valueOf(b.getOrDefault("lon", "0")));
            if (n.isBlank() || lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                throw new Exception("Invalid city details.");
            }
            int cityId = 0;
            try (Connection c = Database.getConnection()) {
                // Check if city already exists
                try (PreparedStatement check = c.prepareStatement(
                        "SELECT city_id FROM Cities WHERE (city_name = ? OR (ABS(latitude - ?) < 0.05 AND ABS(longitude - ?) < 0.05)) AND is_active = 1")) {
                    check.setString(1, n);
                    check.setDouble(2, lat);
                    check.setDouble(3, lon);
                    try (ResultSet cr = check.executeQuery()) {
                        if (cr.next()) {
                            cityId = cr.getInt("city_id");
                        }
                    }
                }
                if (cityId == 0) {
                    try (PreparedStatement p = c.prepareStatement(
                            "INSERT INTO Cities (city_name, country, latitude, longitude, timezone, is_active) VALUES (?, ?, ?, ?, 'Asia/Kolkata', 1)",
                            Statement.RETURN_GENERATED_KEYS)) {
                        p.setString(1, n);
                        p.setString(2, cc);
                        p.setDouble(3, lat);
                        p.setDouble(4, lon);
                        p.executeUpdate();
                        try (ResultSet gk = p.getGeneratedKeys()) {
                            if (gk.next()) cityId = gk.getInt(1);
                        }
                    }
                }
                // Associate with UserCities
                if (cityId > 0 && userId > 0) {
                    try (PreparedStatement puc = c.prepareStatement(
                            "INSERT OR IGNORE INTO UserCities (user_id, city_id) VALUES (?, ?)")) {
                        puc.setInt(1, userId);
                        puc.setInt(2, cityId);
                        puc.executeUpdate();
                    }
                }
            }
            Json.write(res, Map.of("ok", true, "id", cityId, "name", n, "country", cc, "lat", lat, "lon", lon, "isFavorite", false));
        } catch (Exception e) {
            res.setStatus(400);
            Json.write(res, Map.of("ok", false, "message", e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            HttpSession s = requireSession(req, res);
            int userId = (int) s.getAttribute("UserID");
            int id = Integer.parseInt(req.getParameter("id"));
            try (Connection c = Database.getConnection()) {
                // Remove user city association
                try (PreparedStatement p = c.prepareStatement("DELETE FROM UserCities WHERE user_id = ? AND city_id = ?")) {
                    p.setInt(1, userId);
                    p.setInt(2, id);
                    p.executeUpdate();
                }
            }
            Json.write(res, Map.of("ok", true));
        } catch (Exception e) {
            res.setStatus(400);
            Json.write(res, Map.of("ok", false, "message", e.getMessage()));
        }
    }
}
