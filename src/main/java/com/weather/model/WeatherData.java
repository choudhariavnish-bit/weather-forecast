package com.weather.model;

import java.util.ArrayList;
import java.util.List;

public class WeatherData {
    private City city;
    private double currentTempC;
    private double feelsLikeC;
    private int humidity;
    private int pressure;
    private double windSpeedMps;
    private double windSpeedKmh;
    private int clouds;
    private int visibility;
    private String description;
    private String icon;
    private double rainfallMm;
    private List<HourlyForecast> hourlyForecasts = new ArrayList<>();
    private List<DailyForecast> dailyForecasts = new ArrayList<>();
    private List<WeatherAlert> alerts = new ArrayList<>();
    private String updatedAt;

    public WeatherData() {}

    public City getCity() { return city; }
    public void setCity(City city) { this.city = city; }

    public double getCurrentTempC() { return currentTempC; }
    public void setCurrentTempC(double currentTempC) { this.currentTempC = currentTempC; }

    public double getFeelsLikeC() { return feelsLikeC; }
    public void setFeelsLikeC(double feelsLikeC) { this.feelsLikeC = feelsLikeC; }

    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }

    public int getPressure() { return pressure; }
    public void setPressure(int pressure) { this.pressure = pressure; }

    public double getWindSpeedMps() { return windSpeedMps; }
    public void setWindSpeedMps(double windSpeedMps) { this.windSpeedMps = windSpeedMps; }

    public double getWindSpeedKmh() { return windSpeedKmh; }
    public void setWindSpeedKmh(double windSpeedKmh) { this.windSpeedKmh = windSpeedKmh; }

    public int getClouds() { return clouds; }
    public void setClouds(int clouds) { this.clouds = clouds; }

    public int getVisibility() { return visibility; }
    public void setVisibility(int visibility) { this.visibility = visibility; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public double getRainfallMm() { return rainfallMm; }
    public void setRainfallMm(double rainfallMm) { this.rainfallMm = rainfallMm; }

    public List<HourlyForecast> getHourlyForecasts() { return hourlyForecasts; }
    public void setHourlyForecasts(List<HourlyForecast> hourlyForecasts) { this.hourlyForecasts = hourlyForecasts; }

    public List<DailyForecast> getDailyForecasts() { return dailyForecasts; }
    public void setDailyForecasts(List<DailyForecast> dailyForecasts) { this.dailyForecasts = dailyForecasts; }

    public List<WeatherAlert> getAlerts() { return alerts; }
    public void setAlerts(List<WeatherAlert> alerts) { this.alerts = alerts; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
