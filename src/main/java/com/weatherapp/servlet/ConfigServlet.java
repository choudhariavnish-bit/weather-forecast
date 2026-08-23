package com.weatherapp.servlet;

import com.weatherapp.config.AppConfig;
import com.weatherapp.util.Json;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/config")
public class ConfigServlet extends HttpServlet {
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        AppConfig.initialize(config.getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Json.write(res, Map.of("googleMapsApiKey", AppConfig.get("GOOGLE_MAPS_API_KEY")));
    }
}
