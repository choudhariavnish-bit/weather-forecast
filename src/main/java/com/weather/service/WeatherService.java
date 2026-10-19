package com.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.weather.api.OpenWeatherClient;
import com.weather.dao.AlertDAO;
import com.weather.dao.CityDAO;
import com.weather.model.*;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

public class WeatherService {
    private final OpenWeatherClient client;
    private final CityDAO cityDAO;
    private final AlertDAO alertDAO;
    private final CacheService cacheService;

    public WeatherService() {
        this.client = new OpenWeatherClient();
        this.cityDAO = new CityDAO();
        this.alertDAO = new AlertDAO();
        this.cacheService = new CacheService();
    }

    public WeatherData getWeatherData(int cityId) throws Exception {
        String cacheKey = "city_" + cityId;
        WeatherData cached = cacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        City city = cityDAO.findById(cityId);
        if (city == null) {
            throw new IllegalArgumentException("City with ID " + cityId + " not found.");
        }
        WeatherData data = fetchAndProcess(city);
        cacheService.put(cacheKey, data);
        return data;
    }

    public WeatherData getWeatherDataByCoords(double lat, double lon) throws Exception {
        String cacheKey = String.format(Locale.US, "coords_%.4f_%.4f", lat, lon);
        WeatherData cached = cacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        City tempCity = new City(0, "Selected Location", "India", lat, lon);
        WeatherData data = fetchAndProcess(tempCity);
        cacheService.put(cacheKey, data);
        return data;
    }

    private WeatherData fetchAndProcess(City city) throws Exception {
        JsonNode wx = client.getCurrentWeather(city.getLatitude(), city.getLongitude());
        JsonNode fc = client.getForecast(city.getLatitude(), city.getLongitude());

        WeatherData data = new WeatherData();
        data.setCity(city);
        data.setUpdatedAt(Instant.now().toString());

        // Process Current Weather
        double curTemp = wx.path("main").path("temp").asDouble(0.0);
        data.setCurrentTempC(curTemp);
        data.setFeelsLikeC(wx.path("main").path("feels_like").asDouble(curTemp));
        data.setHumidity(wx.path("main").path("humidity").asInt(0));
        data.setPressure(wx.path("main").path("pressure").asInt(1013));
        double speed = wx.path("wind").path("speed").asDouble(0.0);
        data.setWindSpeedMps(speed);
        data.setWindSpeedKmh(Math.round(speed * 3.6 * 10.0) / 10.0);
        data.setClouds(wx.path("clouds").path("all").asInt(0));
        data.setVisibility(wx.path("visibility").asInt(10000));

        if (wx.has("weather") && wx.get("weather").isArray() && !wx.get("weather").isEmpty()) {
            JsonNode w0 = wx.get("weather").get(0);
            data.setDescription(w0.path("description").asText("Clear"));
            data.setIcon(w0.path("icon").asText("01d"));
        } else {
            data.setDescription("Clear");
            data.setIcon("01d");
        }

        double curRain = 0.0;
        if (wx.has("rain")) {
            curRain = wx.get("rain").path("1h").asDouble(wx.get("rain").path("3h").asDouble(0.0));
        }
        data.setRainfallMm(curRain);

        // Process Hourly Forecasts (Next 24 Hours)
        if (fc.has("list")) {
            int count = 0;
            for (JsonNode item : fc.get("list")) {
                if (count >= 8) break;
                long dt = item.path("dt").asLong();
                String dtTxt = item.path("dt_txt").asText();
                double temp = item.path("main").path("temp").asDouble(0.0);
                double pop = item.path("pop").asDouble(0.0);
                double rain1h = 0.0;
                if (item.has("rain")) {
                    rain1h = item.get("rain").path("3h").asDouble(0.0) / 3.0;
                }
                String desc = "";
                String icon = "";
                if (item.has("weather") && item.get("weather").isArray() && !item.get("weather").isEmpty()) {
                    desc = item.get("weather").get(0).path("description").asText("");
                    icon = item.get("weather").get(0).path("icon").asText("");
                }
                data.getHourlyForecasts().add(new HourlyForecast(dt, dtTxt, temp, pop, Math.round(rain1h * 100.0) / 100.0, desc, icon));
                count++;
            }
        }

        // Process Daily Forecasts (7 Days)
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
                double p = it.path("pop").asDouble(0.0);
                if (p > maxPop) maxPop = p;
                if (it.has("rain")) totalRain += it.get("rain").path("3h").asDouble(0.0);
            }

            String desc = "";
            String icon = "";
            if (mid.has("weather") && mid.get("weather").isArray() && !mid.get("weather").isEmpty()) {
                desc = mid.get("weather").get(0).path("description").asText("");
                icon = mid.get("weather").get(0).path("icon").asText("");
            }

            DailyForecast df = new DailyForecast(
                entry.getKey(),
                dtVal,
                dayTemp,
                minTemp == Double.MAX_VALUE ? 0.0 : minTemp,
                maxTemp == -Double.MAX_VALUE ? 0.0 : maxTemp,
                mornTemp,
                nightTemp,
                maxPop,
                Math.round(totalRain * 10.0) / 10.0,
                desc,
                icon
            );
            data.getDailyForecasts().add(df);
        }

        // Process Alerts
        if (wx.has("alerts") && wx.get("alerts").isArray()) {
            for (JsonNode an : wx.get("alerts")) {
                WeatherAlert alert = new WeatherAlert(
                    city.getCityId(),
                    an.path("event").asText("Weather Warning"),
                    an.path("severity").asText("Warning"),
                    an.path("description").asText(""),
                    an.path("start").asLong(Instant.now().getEpochSecond()),
                    an.path("end").asLong(Instant.now().getEpochSecond() + 86400)
                );
                alert.setProviderAlertId(an.path("sender_name").asText("Meteorological Service"));
                data.getAlerts().add(alert);
                if (city.getCityId() > 0) alertDAO.saveAlert(alert);
            }
        }

        return data;
    }
}
