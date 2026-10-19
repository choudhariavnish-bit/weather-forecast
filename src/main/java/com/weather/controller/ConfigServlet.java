package com.weather.controller;

import com.weather.config.AppConfig;
import com.weather.util.JsonUtil;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/app-config")
public class ConfigServlet extends HttpServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        AppConfig.initialize(config.getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String mapsKey = AppConfig.get("GOOGLE_MAPS_API_KEY");
        JsonUtil.sendJsonResponse(res, Map.of("googleMapsApiKey", mapsKey != null ? mapsKey : ""));
    }
}
