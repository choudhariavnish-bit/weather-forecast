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
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            HttpSession s = requireSession(req, res);
            int userId = (int) s.getAttribute("UserID");
            String sql = "SELECT u.UserID,u.FullName,u.Mobile,u.Email,u.CityID,c.CityName,c.CountryCode " +
                         "FROM Users u LEFT JOIN Cities c ON u.CityID=c.CityID WHERE u.UserID=?";
            try (Connection c = Database.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
                p.setInt(1, userId);
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next()) throw new Exception("User profile not found.");
                    Json.write(res, Map.of(
                        "id", r.getInt("UserID"),
                        "name", r.getString("FullName"),
                        "mobile", r.getString("Mobile"),
                        "email", r.getString("Email"),
                        "cityId", r.getInt("CityID"),
                        "city", r.getString("CityName"),
                        "country", r.getString("CountryCode")
                    ));
                }
            }
        } catch (Exception e) {
            res.setStatus(401);
            Json.write(res, Map.of("message", e.getMessage()));
        }
    }
}
