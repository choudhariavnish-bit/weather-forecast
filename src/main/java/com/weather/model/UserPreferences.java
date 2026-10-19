package com.weather.model;

public class UserPreferences {
    private int preferenceId;
    private int userId;
    private String temperatureUnit;
    private String theme;
    private int defaultCityId;
    private int rainAlertThreshold;
    private boolean notificationsEnabled;

    public UserPreferences() {
        this.temperatureUnit = "C";
        this.theme = "light";
        this.rainAlertThreshold = 70;
        this.notificationsEnabled = true;
    }

    public UserPreferences(int userId, String temperatureUnit, String theme, int defaultCityId, int rainAlertThreshold, boolean notificationsEnabled) {
        this.userId = userId;
        this.temperatureUnit = temperatureUnit;
        this.theme = theme;
        this.defaultCityId = defaultCityId;
        this.rainAlertThreshold = rainAlertThreshold;
        this.notificationsEnabled = notificationsEnabled;
    }

    public int getPreferenceId() { return preferenceId; }
    public void setPreferenceId(int preferenceId) { this.preferenceId = preferenceId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTemperatureUnit() { return temperatureUnit; }
    public void setTemperatureUnit(String temperatureUnit) { this.temperatureUnit = temperatureUnit; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public int getDefaultCityId() { return defaultCityId; }
    public void setDefaultCityId(int defaultCityId) { this.defaultCityId = defaultCityId; }

    public int getRainAlertThreshold() { return rainAlertThreshold; }
    public void setRainAlertThreshold(int rainAlertThreshold) { this.rainAlertThreshold = rainAlertThreshold; }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }
}
