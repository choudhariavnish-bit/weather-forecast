package com.weather.service;

import com.weather.dao.CityDAO;
import com.weather.model.City;
import com.weather.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

public class CityService {
    private final CityDAO cityDAO;

    public CityService() {
        this.cityDAO = new CityDAO();
    }

    public List<City> getUserCities(int userId) throws SQLException {
        return cityDAO.findAll(userId);
    }

    public City getCityById(int cityId) throws SQLException {
        return cityDAO.findById(cityId);
    }

    public int addCity(String name, String country, double lat, double lon, int userId) throws Exception {
        if (!ValidationUtil.isNonEmpty(name)) {
            throw new IllegalArgumentException("City name cannot be blank.");
        }
        if (!ValidationUtil.isValidCoordinates(lat, lon)) {
            throw new IllegalArgumentException("Invalid geographic coordinates: [" + lat + ", " + lon + "]");
        }
        City city = new City(0, ValidationUtil.sanitize(name), ValidationUtil.sanitize(country), lat, lon);
        return cityDAO.insert(city, userId);
    }

    public boolean removeCity(int cityId, int userId) throws SQLException {
        return cityDAO.delete(cityId, userId);
    }

    public boolean toggleFavorite(int cityId, int userId) throws SQLException {
        return cityDAO.toggleFavorite(cityId, userId);
    }
}
