package com.weatherapp.servlet;

import com.weatherapp.db.Database;
import com.weatherapp.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.Map;

@WebServlet("/api/profile")
public class ProfileServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            HttpSession s = requireSession(req, res);
            int userId = (int) s.getAttribute("UserID");
            String sql = "SELECT u.user_id, u.full_name, u.mobile, u.email, u.city_id, c.city_name, c.country " +
                         "FROM Users u LEFT JOIN Cities c ON u.city_id = c.city_id WHERE u.user_id = ?";
            try (Connection c = Database.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
                p.setInt(1, userId);
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next()) throw new Exception("User profile not found.");
                    Json.write(res, Map.of(
                        "id", r.getInt("user_id"),
                        "name", r.getString("full_name"),
                        "mobile", r.getString("mobile") != null ? r.getString("mobile") : "",
                        "email", r.getString("email"),
                        "cityId", r.getInt("city_id"),
                        "city", r.getString("city_name") != null ? r.getString("city_name") : "",
                        "country", r.getString("country") != null ? r.getString("country") : ""
                    ));
                }
            }
        } catch (Exception e) {
            res.setStatus(401);
            Json.write(res, Map.of("message", e.getMessage()));
        }
    }
}
