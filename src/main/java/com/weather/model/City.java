package com.weather.model;

public class City {
    private int cityId;
    private String cityName;
    private String countryCode;
    private double latitude;
    private double longitude;
    private String timezone;
    private boolean isActive;
    private boolean isFavorite;

    public City() {}

    public City(int cityId, String cityName, String countryCode, double latitude, double longitude) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.countryCode = countryCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timezone = "Asia/Kolkata";
        this.isActive = true;
    }

    public int getCityId() { return cityId; }
    public void setCityId(int cityId) { this.cityId = cityId; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    @Override
    public String toString() {
        return "City{" + "cityId=" + cityId + ", cityName='" + cityName + '\'' + ", countryCode='" + countryCode + '\'' + ", lat=" + latitude + ", lon=" + longitude + '}';
    }
}
