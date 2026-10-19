package com.weather.model;

public class WeatherAlert {
    private int alertId;
    private int cityId;
    private String cityName;
    private String providerAlertId;
    private String eventType;
    private String severity;
    private String description;
    private long startTime;
    private long endTime;
    private String retrievedAt;
    private String fingerprint;
    private boolean isActive;

    public WeatherAlert() {}

    public WeatherAlert(int cityId, String eventType, String severity, String description, long startTime, long endTime) {
        this.cityId = cityId;
        this.eventType = eventType;
        this.severity = severity;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isActive = true;
    }

    public int getAlertId() { return alertId; }
    public void setAlertId(int alertId) { this.alertId = alertId; }

    public int getCityId() { return cityId; }
    public void setCityId(int cityId) { this.cityId = cityId; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public String getProviderAlertId() { return providerAlertId; }
    public void setProviderAlertId(String providerAlertId) { this.providerAlertId = providerAlertId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }

    public String getRetrievedAt() { return retrievedAt; }
    public void setRetrievedAt(String retrievedAt) { this.retrievedAt = retrievedAt; }

    public String getFingerprint() { return fingerprint; }
    public void setFingerprint(String fingerprint) { this.fingerprint = fingerprint; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
