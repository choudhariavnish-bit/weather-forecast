package com.weather.model;

public class DailyForecast {
    private String date;
    private long dt;
    private double dayTempC;
    private double minTempC;
    private double maxTempC;
    private double morningTempC;
    private double nightTempC;
    private double precipitationProbability;
    private double rainfallMm;
    private String description;
    private String icon;

    public DailyForecast() {}

    public DailyForecast(String date, long dt, double dayTempC, double minTempC, double maxTempC, double morningTempC, double nightTempC, double precipitationProbability, double rainfallMm, String description, String icon) {
        this.date = date;
        this.dt = dt;
        this.dayTempC = dayTempC;
        this.minTempC = minTempC;
        this.maxTempC = maxTempC;
        this.morningTempC = morningTempC;
        this.nightTempC = nightTempC;
        this.precipitationProbability = precipitationProbability;
        this.rainfallMm = rainfallMm;
        this.description = description;
        this.icon = icon;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public long getDt() { return dt; }
    public void setDt(long dt) { this.dt = dt; }

    public double getDayTempC() { return dayTempC; }
    public void setDayTempC(double dayTempC) { this.dayTempC = dayTempC; }

    public double getMinTempC() { return minTempC; }
    public void setMinTempC(double minTempC) { this.minTempC = minTempC; }

    public double getMaxTempC() { return maxTempC; }
    public void setMaxTempC(double maxTempC) { this.maxTempC = maxTempC; }

    public double getMorningTempC() { return morningTempC; }
    public void setMorningTempC(double morningTempC) { this.morningTempC = morningTempC; }

    public double getNightTempC() { return nightTempC; }
    public void setNightTempC(double nightTempC) { this.nightTempC = nightTempC; }

    public double getPrecipitationProbability() { return precipitationProbability; }
    public void setPrecipitationProbability(double precipitationProbability) { this.precipitationProbability = precipitationProbability; }

    public double getRainfallMm() { return rainfallMm; }
    public void setRainfallMm(double rainfallMm) { this.rainfallMm = rainfallMm; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}
