package com.weatherapp.servlet;

import com.weatherapp.db.Database;
import com.weatherapp.security.PasswordUtil;
import com.weatherapp.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.Map;

@WebServlet("/api/login")
public class LoginServlet extends BaseServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            Map<String, Object> b = Json.M.readValue(Json.body(req), Map.class);
            String loginId = String.valueOf(b.getOrDefault("mobile", "")).trim();
            if (loginId.isBlank()) {
                loginId = String.valueOf(b.getOrDefault("email", "")).trim();
            }
            String pw = String.valueOf(b.getOrDefault("password", ""));
            if (loginId.isBlank() || pw.isBlank()) {
                throw new Exception("Mobile number/email and password are required.");
            }
            try (Connection c = Database.getConnection();
                 PreparedStatement p = c.prepareStatement(
                     "SELECT user_id, full_name, password_hash, password_salt, city_id FROM Users WHERE (mobile = ? OR email = ?) AND is_active = 1")) {
                p.setString(1, loginId);
                p.setString(2, loginId);
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next() || !PasswordUtil.matches(pw, r.getString("password_salt"), r.getString("password_hash"))) {
                        throw new Exception("Invalid credentials.");
                    }
                    HttpSession s = req.getSession(true);
                    s.setAttribute("UserID", r.getInt("user_id"));
                    s.setAttribute("UserName", r.getString("full_name"));
                    s.setAttribute("UserCityID", r.getInt("city_id"));
                    Json.write(res, Map.of("ok", true));
                }
            }
        } catch (Exception e) {
            res.setStatus(400);
            Json.write(res, Map.of("ok", false, "message", e.getMessage()));
        }
    }
}
