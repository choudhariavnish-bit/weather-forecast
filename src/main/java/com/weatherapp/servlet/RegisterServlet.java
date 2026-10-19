package com.weatherapp.servlet;

import com.weatherapp.db.Database;
import com.weatherapp.security.PasswordUtil;
import com.weatherapp.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.Map;

@WebServlet("/api/register")
public class RegisterServlet extends BaseServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            Map<String, Object> b = Json.M.readValue(Json.body(req), Map.class);
            String name = String.valueOf(b.getOrDefault("fullName", "")).trim();
            String mobile = String.valueOf(b.getOrDefault("mobile", "")).trim();
            String email = String.valueOf(b.getOrDefault("email", "")).trim();
            String pw = String.valueOf(b.getOrDefault("password", ""));
            int city = Integer.parseInt(String.valueOf(b.getOrDefault("cityId", "0")));

            if (name.isBlank() || mobile.isBlank() || email.isBlank() || city <= 0 || pw.length() < 6) {
                throw new Exception("Enter all details; password must be at least 6 characters.");
            }

            try (Connection c = Database.getConnection()) {
                try (PreparedStatement q = c.prepareStatement(
                        "SELECT COUNT(*) FROM Users WHERE (mobile = ? OR email = ?) AND is_active = 1")) {
                    q.setString(1, mobile);
                    q.setString(2, email);
                    try (ResultSet r = q.executeQuery()) {
                        r.next();
                        if (r.getInt(1) > 0) {
                            throw new Exception("Mobile number or email is already registered.");
                        }
                    }
                }

                String salt = PasswordUtil.salt();
                String hash = PasswordUtil.hash(pw, salt);

                try (PreparedStatement p = c.prepareStatement(
                        "INSERT INTO Users (full_name, mobile, email, city_id, password_hash, password_salt, is_active) VALUES (?, ?, ?, ?, ?, ?, 1)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    p.setString(1, name);
                    p.setString(2, mobile);
                    p.setString(3, email);
                    p.setInt(4, city);
                    p.setString(5, hash);
                    p.setString(6, salt);
                    p.executeUpdate();

                    try (ResultSet gk = p.getGeneratedKeys()) {
                        if (gk.next()) {
                            int newUserId = gk.getInt(1);
                            try (PreparedStatement puc = c.prepareStatement(
                                    "INSERT OR IGNORE INTO UserCities (user_id, city_id) VALUES (?, ?)")) {
                                puc.setInt(1, newUserId);
                                puc.setInt(2, city);
                                puc.executeUpdate();
                            }
                        }
                    }
                }
            }
            Json.write(res, Map.of("ok", true));
        } catch (Exception e) {
            res.setStatus(400);
            Json.write(res, Map.of("ok", false, "message", e.getMessage()));
        }
    }
}
