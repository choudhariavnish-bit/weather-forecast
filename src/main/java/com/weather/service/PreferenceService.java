package com.weather.service;

import com.weather.dao.PreferenceDAO;
import com.weather.model.UserPreferences;

import java.sql.SQLException;

public class PreferenceService {
    private final PreferenceDAO preferenceDAO;

    public PreferenceService() {
        this.preferenceDAO = new PreferenceDAO();
    }

    public UserPreferences getUserPreferences(int userId) throws SQLException {
        return preferenceDAO.findByUserId(userId);
    }

    public boolean updatePreferences(UserPreferences prefs) throws SQLException {
        if (!"F".equalsIgnoreCase(prefs.getTemperatureUnit())) {
            prefs.setTemperatureUnit("C");
        } else {
            prefs.setTemperatureUnit("F");
        }

        if (!"dark".equalsIgnoreCase(prefs.getTheme())) {
            prefs.setTheme("light");
        } else {
            prefs.setTheme("dark");
        }

        if (prefs.getRainAlertThreshold() < 0) prefs.setRainAlertThreshold(0);
        if (prefs.getRainAlertThreshold() > 100) prefs.setRainAlertThreshold(100);

        return preferenceDAO.saveOrUpdate(prefs);
    }
}
