package com.weather.controller;

import com.weather.model.WeatherData;
import com.weather.service.WeatherService;
import com.weather.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/weather-data")
public class WeatherServlet extends HttpServlet {
    private final WeatherService weatherService = new WeatherService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            String cityParam = req.getParameter("cityId");
            String latParam = req.getParameter("lat");
            String lonParam = req.getParameter("lon");

            WeatherData weatherData;
            if (latParam != null && lonParam != null) {
                double lat = Double.parseDouble(latParam);
                double lon = Double.parseDouble(lonParam);
                weatherData = weatherService.getWeatherDataByCoords(lat, lon);
            } else {
                int cityId = 1;
                if (cityParam != null && !cityParam.isBlank()) {
                    cityId = Integer.parseInt(cityParam);
                } else {
                    HttpSession session = req.getSession(false);
                    if (session != null && session.getAttribute("UserCityID") != null) {
                        cityId = (int) session.getAttribute("UserCityID");
                    }
                }
                weatherData = weatherService.getWeatherData(cityId);
            }

            JsonUtil.sendJsonResponse(res, weatherData);
        } catch (Exception e) {
            res.setStatus(400);
            Map<String, Object> err = new HashMap<>();
            err.put("ok", false);
            err.put("message", e.getMessage());
            JsonUtil.sendJsonResponse(res, err);
        }
    }
}
