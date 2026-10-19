package com.weather.model;

public class HourlyForecast {
    private long dt;
    private String time;
    private double temperatureC;
    private double precipitationProbability;
    private double rainfallMm;
    private String description;
    private String icon;

    public HourlyForecast() {}

    public HourlyForecast(long dt, String time, double temperatureC, double precipitationProbability, double rainfallMm, String description, String icon) {
        this.dt = dt;
        this.time = time;
        this.temperatureC = temperatureC;
        this.precipitationProbability = precipitationProbability;
        this.rainfallMm = rainfallMm;
        this.description = description;
        this.icon = icon;
    }

    public long getDt() { return dt; }
    public void setDt(long dt) { this.dt = dt; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public double getTemperatureC() { return temperatureC; }
    public void setTemperatureC(double temperatureC) { this.temperatureC = temperatureC; }

    public double getPrecipitationProbability() { return precipitationProbability; }
    public void setPrecipitationProbability(double precipitationProbability) { this.precipitationProbability = precipitationProbability; }

    public double getRainfallMm() { return rainfallMm; }
    public void setRainfallMm(double rainfallMm) { this.rainfallMm = rainfallMm; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}
