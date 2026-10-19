package com.weather.service;

import com.weather.dao.AlertDAO;
import com.weather.model.WeatherAlert;

import java.sql.SQLException;
import java.util.List;

public class AlertService {
    private final AlertDAO alertDAO;

    public AlertService() {
        this.alertDAO = new AlertDAO();
    }

    public List<WeatherAlert> getActiveAlerts(int cityId) throws SQLException {
        return alertDAO.findActiveAlerts(cityId);
    }

    public List<WeatherAlert> getAlertHistory(int cityId, int limit) throws SQLException {
        return alertDAO.findAlertHistory(cityId, limit);
    }

    public boolean recordAlert(WeatherAlert alert) {
        return alertDAO.saveAlert(alert);
    }
}
