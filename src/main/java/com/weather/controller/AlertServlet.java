package com.weather.controller;

import com.weather.model.WeatherAlert;
import com.weather.service.AlertService;
import com.weather.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/alert-controller")
public class AlertServlet extends HttpServlet {
    private final AlertService alertService = new AlertService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            String cityParam = req.getParameter("cityId");
            String historyParam = req.getParameter("history");
            int cityId = cityParam != null && !cityParam.isBlank() ? Integer.parseInt(cityParam) : 0;
            boolean history = "true".equalsIgnoreCase(historyParam);

            List<WeatherAlert> alerts;
            if (history) {
                alerts = alertService.getAlertHistory(cityId, 50);
            } else {
                alerts = alertService.getActiveAlerts(cityId);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("ok", true);
            result.put("history", history);
            result.put("cityId", cityId);
            result.put("count", alerts.size());
            result.put("alerts", alerts);
            JsonUtil.sendJsonResponse(res, result);
        } catch (Exception e) {
            res.setStatus(400);
            Map<String, Object> err = new HashMap<>();
            err.put("ok", false);
            err.put("message", e.getMessage());
            JsonUtil.sendJsonResponse(res, err);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            WeatherAlert alert = JsonUtil.MAPPER.readValue(JsonUtil.readRequestBody(req), WeatherAlert.class);
            boolean saved = alertService.recordAlert(alert);
            Map<String, Object> result = new HashMap<>();
            result.put("ok", saved);
            result.put("message", saved ? "Alert recorded successfully" : "Failed to record alert");
            JsonUtil.sendJsonResponse(res, result);
        } catch (Exception e) {
            res.setStatus(400);
            Map<String, Object> err = new HashMap<>();
            err.put("ok", false);
            err.put("message", e.getMessage());
            JsonUtil.sendJsonResponse(res, err);
        }
    }
}
