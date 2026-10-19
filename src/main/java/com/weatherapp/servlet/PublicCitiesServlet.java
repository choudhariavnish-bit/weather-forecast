package com.weatherapp.servlet;

import com.weatherapp.db.Database;
import com.weatherapp.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/api/public-cities")
public class PublicCitiesServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            List<Map<String, Object>> a = new ArrayList<>();
            try (Connection c = Database.getConnection();
                 PreparedStatement p = c.prepareStatement(
                     "SELECT city_id, city_name, country FROM Cities WHERE is_active = 1 ORDER BY city_name");
                 ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    a.add(Map.of(
                        "id", r.getInt("city_id"),
                        "name", r.getString("city_name"),
                        "country", r.getString("country")
                    ));
                }
            }
            Json.write(res, a);
        } catch (Exception e) {
            res.setStatus(500);
            Json.write(res, Map.of("message", e.getMessage()));
        }
    }
}
