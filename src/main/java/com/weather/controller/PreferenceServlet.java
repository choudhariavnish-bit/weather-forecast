package com.weather.controller;

import com.weather.model.UserPreferences;
import com.weather.service.PreferenceService;
import com.weather.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/user-preferences")
public class PreferenceServlet extends HttpServlet {
    private final PreferenceService preferenceService = new PreferenceService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            int userId = 1;
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("UserID") != null) {
                userId = (int) session.getAttribute("UserID");
            }
            UserPreferences prefs = preferenceService.getUserPreferences(userId);
            JsonUtil.sendJsonResponse(res, prefs);
        } catch (Exception e) {
            res.setStatus(400);
            JsonUtil.sendJsonResponse(res, Map.of("ok", false, "message", e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        doPut(req, res);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            int userId = 1;
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("UserID") != null) {
                userId = (int) session.getAttribute("UserID");
            }

            Map<String, Object> body = JsonUtil.MAPPER.readValue(JsonUtil.readRequestBody(req), Map.class);
            UserPreferences prefs = new UserPreferences();
            prefs.setUserId(userId);
            prefs.setTemperatureUnit(String.valueOf(body.getOrDefault("temperatureUnit", "C")));
            prefs.setTheme(String.valueOf(body.getOrDefault("theme", "light")));
            prefs.setDefaultCityId(Integer.parseInt(String.valueOf(body.getOrDefault("defaultCityId", "1"))));
            prefs.setRainAlertThreshold(Integer.parseInt(String.valueOf(body.getOrDefault("rainAlertThreshold", "70"))));
            prefs.setNotificationsEnabled(Boolean.parseBoolean(String.valueOf(body.getOrDefault("notificationsEnabled", "true"))));

            boolean ok = preferenceService.updatePreferences(prefs);
            JsonUtil.sendJsonResponse(res, Map.of("ok", ok, "preferences", prefs));
        } catch (Exception e) {
            res.setStatus(400);
            JsonUtil.sendJsonResponse(res, Map.of("ok", false, "message", e.getMessage()));
        }
    }
}
