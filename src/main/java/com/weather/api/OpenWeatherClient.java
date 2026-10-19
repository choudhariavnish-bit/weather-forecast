package com.weather.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.weather.config.AppConfig;
import com.weather.util.JsonUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class OpenWeatherClient {
    private final HttpClient httpClient;

    public OpenWeatherClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public JsonNode getCurrentWeather(double lat, double lon) throws IOException, InterruptedException {
        String apiKey = getValidApiKey();
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat +
                "&lon=" + lon + "&units=metric&appid=" + encode(apiKey);
        return fetchJson(url);
    }

    public JsonNode getForecast(double lat, double lon) throws IOException, InterruptedException {
        String apiKey = getValidApiKey();
        String url = "https://api.openweathermap.org/data/2.5/forecast?lat=" + lat +
                "&lon=" + lon + "&units=metric&appid=" + encode(apiKey);
        return fetchJson(url);
    }

    private JsonNode fetchJson(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "WeatherDashboard-Java/1.0")
                .timeout(Duration.ofSeconds(12))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IOException("OpenWeather API error: HTTP " + response.statusCode() + " (" + response.body() + ")");
        }
        return JsonUtil.MAPPER.readTree(response.body());
    }

    private String getValidApiKey() throws IOException {
        String key = AppConfig.get("OPENWEATHER_API_KEY");
        if (key == null || key.isBlank() || key.contains("PUT_YOUR") || key.equals("YOUR_OPENWEATHER_API_KEY")) {
            throw new IOException("Valid OPENWEATHER_API_KEY is not configured.");
        }
        return key;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
