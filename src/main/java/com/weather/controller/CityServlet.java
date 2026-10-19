package com.weather.controller;

import com.weather.model.City;
import com.weather.service.CityService;
import com.weather.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/city-controller")
public class CityServlet extends HttpServlet {
    private final CityService cityService = new CityService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            int userId = 1;
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("UserID") != null) {
                userId = (int) session.getAttribute("UserID");
            }
            List<City> cities = cityService.getUserCities(userId);
            JsonUtil.sendJsonResponse(res, cities);
        } catch (Exception e) {
            res.setStatus(400);
            JsonUtil.sendJsonResponse(res, Map.of("ok", false, "message", e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            int userId = 1;
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("UserID") != null) {
                userId = (int) session.getAttribute("UserID");
            }

            Map<String, Object> body = JsonUtil.MAPPER.readValue(JsonUtil.readRequestBody(req), Map.class);
            String action = String.valueOf(body.getOrDefault("action", ""));

            if ("favorite".equalsIgnoreCase(action) || "toggleFavorite".equalsIgnoreCase(action)) {
                int cityId = Integer.parseInt(String.valueOf(body.getOrDefault("cityId", "0")));
                boolean isFav = cityService.toggleFavorite(cityId, userId);
                JsonUtil.sendJsonResponse(res, Map.of("ok", true, "cityId", cityId, "isFavorite", isFav));
                return;
            }

            String name = String.valueOf(body.getOrDefault("name", ""));
            String country = String.valueOf(body.getOrDefault("country", "India"));
            double lat = Double.parseDouble(String.valueOf(body.getOrDefault("lat", "0")));
            double lon = Double.parseDouble(String.valueOf(body.getOrDefault("lon", "0")));

            int id = cityService.addCity(name, country, lat, lon, userId);
            JsonUtil.sendJsonResponse(res, Map.of("ok", true, "id", id, "name", name, "country", country));
        } catch (Exception e) {
            res.setStatus(400);
            JsonUtil.sendJsonResponse(res, Map.of("ok", false, "message", e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            int userId = 1;
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("UserID") != null) {
                userId = (int) session.getAttribute("UserID");
            }
            int cityId = Integer.parseInt(req.getParameter("id"));
            boolean ok = cityService.removeCity(cityId, userId);
            JsonUtil.sendJsonResponse(res, Map.of("ok", ok));
        } catch (Exception e) {
            res.setStatus(400);
            JsonUtil.sendJsonResponse(res, Map.of("ok", false, "message", e.getMessage()));
        }
    }
}
