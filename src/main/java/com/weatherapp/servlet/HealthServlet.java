package com.weatherapp.servlet;

import com.weatherapp.db.Database;
import com.weatherapp.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.Map;

@WebServlet("/api/health")
public class HealthServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        boolean dbOk = false;
        int cityCount = 0;
        try (Connection c = Database.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT COUNT(*) FROM Cities")) {
            if (r.next()) {
                cityCount = r.getInt(1);
                dbOk = true;
            }
        } catch (Exception e) {
            dbOk = false;
        }

        Json.write(res, Map.of(
            "status", dbOk ? "ok" : "degraded",
            "database", dbOk ? "connected" : "error",
            "citiesCount", cityCount,
            "timestamp", Instant.now().toString(),
            "version", "1.0.0"
        ));
    }
}
