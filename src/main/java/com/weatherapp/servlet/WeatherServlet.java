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
import java.security.MessageDigest;
import java.sql.*;
import java.time.Instant;
import java.util.*;

@WebServlet("/api/weather")
public class WeatherServlet extends BaseServlet {
    private final HttpClient http = HttpClient.newHttpClient();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            HttpSession s = requireSession(req, res);
            String cityParam = req.getParameter("cityId");
            String latParam = req.getParameter("lat");
            String lonParam = req.getParameter("lon");

            Map<String, Object> city = null;
            int cityId = 0;
            double lat = 0.0, lon = 0.0;

            if (latParam != null && lonParam != null) {
                lat = Double.parseDouble(latParam);
                lon = Double.parseDouble(lonParam);
                city = Map.of("id", 0, "name", "Selected Location", "country", "India", "lat", lat, "lon", lon);
            } else {
                if (cityParam != null && !cityParam.isBlank()) {
                    cityId = Integer.parseInt(cityParam);
                } else if (s.getAttribute("UserCityID") != null) {
                    cityId = (int) s.getAttribute("UserCityID");
                } else {
                    cityId = 1;
                }

                try (Connection c = Database.getConnection();
                     PreparedStatement p = c.prepareStatement(
                         "SELECT city_id, city_name, country, latitude, longitude FROM Cities WHERE city_id = ? AND is_active = 1")) {
                    p.setInt(1, cityId);
                    try (ResultSet r = p.executeQuery()) {
                        if (r.next()) {
                            city = Map.of(
                                "id", r.getInt("city_id"),
                                "name", r.getString("city_name"),
                                "country", r.getString("country"),
                                "lat", r.getDouble("latitude"),
                                "lon", r.getDouble("longitude")
                            );
                            lat = r.getDouble("latitude");
                            lon = r.getDouble("longitude");
                        }
                    }
                }
            }

            if (city == null) throw new Exception("City not found.");

            String key = AppConfig.get("OPENWEATHER_API_KEY");
            if (key.isBlank() || key.contains("PUT_YOUR") || key.equals("YOUR_OPENWEATHER_API_KEY")) {
                throw new Exception("Please configure a valid OPENWEATHER_API_KEY in WEB-INF/configAPI.xml.");
            }

            String weatherUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&units=metric&appid=" + enc(key);
            String forecastUrl = "https://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&units=metric&appid=" + enc(key);

            JsonNode wx = get(weatherUrl);
            JsonNode fc = get(forecastUrl);

            // Current Weather
            ObjectNode current = Json.M.createObjectNode();
            double curTemp = wx.path("main").path("temp").asDouble(0.0);
            double curFeels = wx.path("main").path("feels_like").asDouble(curTemp);
            int curHumidity = wx.path("main").path("humidity").asInt(0);
            int curPressure = wx.path("main").path("pressure").asInt(1013);
            double curWindSpeed = wx.path("wind").path("speed").asDouble(0.0);
            double curWindKmh = Math.round(curWindSpeed * 3.6 * 10.0) / 10.0;
            int curClouds = wx.path("clouds").path("all").asInt(0);
            int curVisibility = wx.path("visibility").asInt(10000);

            String curDesc = "Clear sky";
            String curIcon = "01d";
            if (wx.has("weather") && wx.get("weather").isArray() && !wx.get("weather").isEmpty()) {
                curDesc = wx.get("weather").get(0).path("description").asText("Clear sky");
                curIcon = wx.get("weather").get(0).path("icon").asText("01d");
            }

            double curRain = 0.0;
            if (wx.has("rain")) {
                curRain = wx.get("rain").path("1h").asDouble(wx.get("rain").path("3h").asDouble(0.0));
            }

            current.put("temp", curTemp);
            current.put("temperatureC", curTemp);
            current.put("feels_like", curFeels);
            current.put("feelsLikeC", curFeels);
            current.put("humidity", curHumidity);
            current.put("pressure", curPressure);
            current.put("wind_speed", curWindSpeed);
            current.put("windSpeedMps", curWindSpeed);
            current.put("windSpeedKmh", curWindKmh);
            current.put("clouds", curClouds);
            current.put("visibility", curVisibility);
            current.put("description", curDesc);
            current.put("icon", curIcon);
            current.put("rainfallMm", curRain);
            current.set("weather", wx.path("weather"));

            // Hourly Forecast (Next 24 Hours from 3-hour list)
            ArrayNode hourlyArr = Json.M.createArrayNode();
            if (fc.has("list")) {
                int count = 0;
                for (JsonNode item : fc.get("list")) {
                    if (count >= 8) break; // 8 * 3h = 24 hours
                    ObjectNode hNode = Json.M.createObjectNode();
                    long dt = item.path("dt").asLong();
                    String dtTxt = item.path("dt_txt").asText();
                    double temp = item.path("main").path("temp").asDouble(0.0);
                    double pop = item.path("pop").asDouble(0.0);
                    double rain1h = 0.0;
                    if (item.has("rain")) {
                        rain1h = item.get("rain").path("3h").asDouble(0.0) / 3.0;
                    }
                    String hDesc = "";
                    String hIcon = "";
                    if (item.has("weather") && item.get("weather").isArray() && !item.get("weather").isEmpty()) {
                        hDesc = item.get("weather").get(0).path("description").asText("");
                        hIcon = item.get("weather").get(0).path("icon").asText("");
                    }
                    hNode.put("dt", dt);
                    hNode.put("time", dtTxt);
                    hNode.put("temperatureC", temp);
                    hNode.put("temp", temp);
                    hNode.put("precipitationProbability", pop);
                    hNode.put("pop", pop);
                    hNode.put("rainfallMm", Math.round(rain1h * 100.0) / 100.0);
                    hNode.put("description", hDesc);
                    hNode.put("icon", hIcon);
                    hourlyArr.add(hNode);
                    count++;
                }
            }

            // Daily Forecast (7 days by aggregating intervals)
            ObjectNode dailyObj = Json.M.createObjectNode();
            ArrayNode dailyArr = Json.M.createArrayNode();
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
                double totalRain = 0.0;

                for (JsonNode it : items) {
                    double t = it.path("main").path("temp").asDouble(0.0);
                    if (t < minTemp) minTemp = t;
                    if (t > maxTemp) maxTemp = t;
                    double pop = it.path("pop").asDouble(0.0);
                    if (pop > maxPop) maxPop = pop;
                    if (it.has("rain")) {
                        totalRain += it.get("rain").path("3h").asDouble(0.0);
                    }
                }

                ObjectNode dNode = Json.M.createObjectNode();
                dNode.put("date", entry.getKey());
                dNode.put("dt", dtVal);
                dNode.set("weather", mid.path("weather"));
                dNode.put("pop", maxPop);
                dNode.put("precipitationProbability", maxPop);
                dNode.put("rainfallMm", Math.round(totalRain * 10.0) / 10.0);

                ObjectNode tempNode = Json.M.createObjectNode();
                tempNode.put("day", dayTemp);
                tempNode.put("min", minTemp == Double.MAX_VALUE ? 0.0 : minTemp);
                tempNode.put("max", maxTemp == -Double.MAX_VALUE ? 0.0 : maxTemp);
                tempNode.put("morn", mornTemp);
                tempNode.put("night", nightTemp);
                dNode.set("temp", tempNode);
                dNode.put("minTemperatureC", minTemp == Double.MAX_VALUE ? 0.0 : minTemp);
                dNode.put("maxTemperatureC", maxTemp == -Double.MAX_VALUE ? 0.0 : maxTemp);

                dailyArr.add(dNode);
            }
            dailyObj.set("data", dailyArr);

            // Weather Alerts Extraction
            List<Map<String, Object>> alertsList = new ArrayList<>();
            if (wx.has("alerts") && wx.get("alerts").isArray()) {
                for (JsonNode aNode : wx.get("alerts")) {
                    alertsList.add(Map.of(
                        "sender_name", aNode.path("sender_name").asText("Weather Authority"),
                        "event", aNode.path("event").asText("Weather Warning"),
                        "severity", aNode.path("severity").asText("Warning"),
                        "start", aNode.path("start").asLong(Instant.now().getEpochSecond()),
                        "end", aNode.path("end").asLong(Instant.now().getEpochSecond() + 86400),
                        "description", aNode.path("description").asText("")
                    ));
                }
            }

            // Save to WeatherHistory & Forecasts table
            if (cityId > 0) {
                final int cId = cityId;
                final double tVal = curTemp;
                final int hVal = curHumidity;
                final double wVal = curWindSpeed;
                final double rVal = curRain;
                final String dVal = curDesc;
                new Thread(() -> {
                    try (Connection c = Database.getConnection()) {
                        try (PreparedStatement pHist = c.prepareStatement(
                                "INSERT INTO WeatherHistory (city_id, temperature, humidity, wind_speed, rainfall_mm, description) VALUES (?, ?, ?, ?, ?, ?)")) {
                            pHist.setInt(1, cId);
                            pHist.setDouble(2, tVal);
                            pHist.setInt(3, hVal);
                            pHist.setDouble(4, wVal);
                            pHist.setDouble(5, rVal);
                            pHist.setString(6, dVal);
                            pHist.executeUpdate();
                        }
                        try (PreparedStatement pFc = c.prepareStatement(
                                "INSERT INTO Forecasts (city_id, temperature, humidity, description, wind_speed) VALUES (?, ?, ?, ?, ?)")) {
                            pFc.setInt(1, cId);
                            pFc.setDouble(2, tVal);
                            pFc.setInt(3, hVal);
                            pFc.setString(4, dVal);
                            pFc.setDouble(5, wVal);
                            pFc.executeUpdate();
                        }
                    } catch (Exception ignored) {}
                }).start();
            }

            Json.write(res, Map.of(
                "city", city,
                "current", current,
                "hourly", hourlyArr,
                "daily", dailyObj,
                "alerts", alertsList,
                "updatedAt", Instant.now().toString()
            ));
        } catch (Exception e) {
            if (res.getStatus() < 400) res.setStatus(400);
            Json.write(res, Map.of("ok", false, "message", e.getMessage()));
        }
    }

    private JsonNode get(String url) throws Exception {
        HttpRequest q = HttpRequest.newBuilder(URI.create(url)).header("User-Agent", "WeatherDashboard-Java/1.0").timeout(java.time.Duration.ofSeconds(10)).GET().build();
        HttpResponse<String> r = http.send(q, HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() / 100 != 2) throw new IOException("OpenWeather API error: HTTP " + r.statusCode() + " (" + r.body() + ")");
        return Json.M.readTree(r.body());
    }

    private String enc(String x) {
        return URLEncoder.encode(x, java.nio.charset.StandardCharsets.UTF_8);
    }
}
