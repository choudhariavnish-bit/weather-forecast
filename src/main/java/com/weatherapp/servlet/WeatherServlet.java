package com.weatherapp.servlet;

import com.weatherapp.config.AppConfig;
import com.weatherapp.db.Database;
import com.weatherapp.util.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.sql.*;
import java.util.*;

@WebServlet("/api/weather")
public class WeatherServlet extends BaseServlet {
    private final HttpClient http = HttpClient.newHttpClient();

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            HttpSession s = requireSession(req, res);
            int cityId = Integer.parseInt(Optional.ofNullable(req.getParameter("cityId")).orElse(String.valueOf(s.getAttribute("UserCityID"))));
            Map<String, Object> city = null;
            try (Connection c = Database.getConnection(); PreparedStatement p = c.prepareStatement("SELECT CityID,CityName,CountryCode,Latitude,Longitude FROM Cities WHERE CityID=? AND IsActive=True")) {
                p.setInt(1, cityId);
                try (ResultSet r = p.executeQuery()) {
                    if (r.next()) city = Map.of("id", r.getInt(1), "name", r.getString(2), "country", r.getString(3), "lat", r.getDouble(4), "lon", r.getDouble(5));
                }
            }
            if (city == null) throw new Exception("City not found.");
            String key = AppConfig.get("OPENWEATHER_API_KEY");
            if (key.isBlank() || key.contains("PUT_YOUR")) throw new Exception("Set valid OPENWEATHER_API_KEY.");
            double lat = (double) city.get("lat"), lon = (double) city.get("lon");

            String weatherUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&units=metric&appid=" + enc(key);
            String forecastUrl = "https://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&units=metric&appid=" + enc(key);

            JsonNode wx = get(weatherUrl);
            JsonNode fc = get(forecastUrl);

            ObjectNode current = Json.M.createObjectNode();
            current.put("temp", wx.path("main").path("temp").asDouble(0.0));
            current.put("feels_like", wx.path("main").path("feels_like").asDouble(0.0));
            current.put("humidity", wx.path("main").path("humidity").asInt(0));
            current.put("pressure", wx.path("main").path("pressure").asInt(0));
            current.put("wind_speed", wx.path("wind").path("speed").asDouble(0.0));
            current.put("clouds", wx.path("clouds").path("all").asInt(0));
            current.put("visibility", wx.path("visibility").asInt(0));
            current.set("weather", wx.path("weather"));

            ObjectNode dailyObj = Json.M.createObjectNode();
            ArrayNode dataArr = Json.M.createArrayNode();

            Map<String, List<JsonNode>> days = new LinkedHashMap<>();
            if (fc.has("list")) {
                for (JsonNode item : fc.get("list")) {
                    String dtTxt = item.path("dt_txt").asText();
                    String dayKey = dtTxt.contains(" ") ? dtTxt.split(" ")[0] : dtTxt;
                    days.computeIfAbsent(dayKey, k -> new ArrayList<>()).add(item);
                }
            }

            for (Map.Entry<String, List<JsonNode>> entry : days.entrySet()) {
                List<JsonNode> items = entry.getValue();
                if (items.isEmpty()) continue;
                JsonNode mid = items.get(items.size() / 2);
                long dtVal = mid.path("dt").asLong();

                double minTemp = Double.MAX_VALUE;
                double maxTemp = -Double.MAX_VALUE;
                double dayTemp = mid.path("main").path("temp").asDouble(0.0);
                double mornTemp = items.get(0).path("main").path("temp").asDouble(dayTemp);
                double nightTemp = items.get(items.size() - 1).path("main").path("temp").asDouble(dayTemp);
                double maxPop = 0.0;

                for (JsonNode it : items) {
                    double t = it.path("main").path("temp").asDouble(0.0);
                    if (t < minTemp) minTemp = t;
                    if (t > maxTemp) maxTemp = t;
                    double pop = it.path("pop").asDouble(0.0);
                    if (pop > maxPop) maxPop = pop;
                }

                ObjectNode dNode = Json.M.createObjectNode();
                dNode.put("dt", dtVal);
                dNode.set("weather", mid.path("weather"));
                dNode.put("pop", maxPop);

                ObjectNode tempNode = Json.M.createObjectNode();
                tempNode.put("day", dayTemp);
                tempNode.put("min", minTemp == Double.MAX_VALUE ? 0.0 : minTemp);
                tempNode.put("max", maxTemp == -Double.MAX_VALUE ? 0.0 : maxTemp);
                tempNode.put("morn", mornTemp);
                tempNode.put("night", nightTemp);
                dNode.set("temp", tempNode);

                dataArr.add(dNode);
            }

            dailyObj.set("data", dataArr);

            Json.write(res, Map.of("city", city, "current", current, "daily", dailyObj, "alerts", Collections.emptyList()));
        } catch (Exception e) {
            if (res.getStatus() < 400) res.setStatus(400);
            Json.write(res, Map.of("ok", false, "message", e.getMessage()));
        }
    }

    private JsonNode get(String url) throws Exception {
        HttpRequest q = HttpRequest.newBuilder(URI.create(url)).header("User-Agent", "WeatherDashboard-Java/1.0").GET().build();
        HttpResponse<String> r = http.send(q, HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() / 100 != 2) throw new IOException("OpenWeather error: HTTP " + r.statusCode() + " " + r.body());
        return Json.M.readTree(r.body());
    }

    private String enc(String x) {
        return URLEncoder.encode(x, java.nio.charset.StandardCharsets.UTF_8);
    }
}

