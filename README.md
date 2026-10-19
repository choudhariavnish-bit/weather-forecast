# Java Mini Project: Weather Dashboard

**Project Title:** Weather Dashboard Using Java, Bootstrap 5, Google Maps and OpenWeather API

A Java-based web application with object-oriented design, JDBC database connectivity, Jakarta Servlets, RESTful JSON endpoints, API integration, and database operations. HTML5, Bootstrap 5, and JavaScript power the responsive user interface.

---

## 1. Technology Stack

| Technology | Role in the Project |
|---|---|
| **Java 21** | Main programming language and backend business logic |
| **Jakarta Servlets (EE10)** | Handle HTTP requests, routing, session management, and JSON responses |
| **Jetty 12** | Embedded web server and servlet container |
| **JDBC + UCanAccess / SQLite** | Dual database engine: connects Java to MS Access (`ForecastDBMaster.mdb`) or SQLite (`ForecastDBMaster.db`) |
| **HTML5 + Bootstrap 5** | Responsive web interface |
| **JavaScript + Chart.js** | Dynamic client updates, interactive temperature, and precipitation charts |
| **OpenWeather API** | Current weather, 24-hour hourly, and 7-day daily forecast data |
| **Google Maps & Places API**| Interactive city selection, pin drop, and geolocation |
| **Maven** | Dependency management, compilation, and WAR packaging |

---

## 2. Java Project Architecture

The backend follows a layered object-oriented architecture:

```
[ Presentation Layer ]  HTML5 · Bootstrap 5 · JavaScript · Chart.js · Google Maps
         │
         ▼
[ Controller Layer ]    Jakarta Servlets · Request Validation · JSON Serialization
         │
         ▼
[ Service Layer ]       WeatherService · CityService · AlertService · PreferenceService
         │
         ▼
[ DAO / Data Layer ]    CityDAO · UserCityDAO · PreferenceDAO · AlertDAO
         │
         ▼
[ Persistence & APIs ]  JDBC (UCanAccess MDB / SQLite) · Java HttpClient (OpenWeather API)
```

---

## 3. Java Package Structure (`src/main/java`)

```
com.weather
├── config
│   ├── AppConfig.java            # Loads XML API keys & environment configs
│   └── DatabaseConnection.java   # JDBC Connection provider (SQLite & MS Access UCanAccess)
├── controller
│   ├── WeatherServlet.java       # Exposes /api/weather-data (24h hourly & 7d daily forecasts)
│   ├── CityServlet.java          # Exposes /api/city-controller (City management & favorites)
│   ├── PreferenceServlet.java    # Exposes /api/user-preferences (Units, theme, threshold)
│   └── AlertServlet.java         # Exposes /api/alert-controller (Active & historical alerts)
├── service
│   ├── WeatherService.java       # Coordinates API client, models, and alerts
│   ├── CityService.java          # Validates & manages cities and favorites
│   ├── PreferenceService.java    # Manages user settings & defaults
│   └── AlertService.java         # Weather alert rules, queries & expiry
├── dao
│   ├── CityDAO.java              # CRUD operations for Cities table
│   ├── UserCityDAO.java          # Relational mapping for UserCities
│   ├── PreferenceDAO.java        # CRUD operations for UserPreferences table
│   └── AlertDAO.java             # Queries & persistence for WeatherAlerts table
├── model
│   ├── City.java                 # City domain entity
│   ├── WeatherData.java          # Aggregated telemetry, hourly & daily lists
│   ├── HourlyForecast.java       # 24-hour interval with temp & precipitation
│   ├── DailyForecast.java        # 7-day forecast with high/low, morning/night, pop
│   ├── UserPreferences.java      # User preference settings (C/F, theme, alerts)
│   └── WeatherAlert.java         # Weather alert domain model
├── api
│   └── OpenWeatherClient.java    # OpenWeather API client using java.net.http.HttpClient
└── util
    ├── JsonUtil.java             # Jackson ObjectMapper serializer/deserializer
    └── ValidationUtil.java       # Coordinate & input validation utilities
```

---

## 4. Database Design & Schema

The application supports both **Microsoft Access (`ForecastDBMaster.mdb`)** and **SQLite (`ForecastDBMaster.db`)**:

| Table | Primary Columns | Description |
|---|---|---|
| `Cities` | `city_id`, `city_name`, `country`, `latitude`, `longitude`, `timezone`, `is_active` | Stores predefined and user-added cities |
| `Users` | `user_id`, `full_name`, `mobile`, `email`, `password_hash`, `password_salt`, `city_id` | User registration and authentication |
| `UserCities` | `user_city_id`, `user_id`, `city_id`, `is_favorite`, `display_order`, `added_at` | M-N mapping of users to favorite cities |
| `UserPreferences` | `preference_id`, `user_id`, `temperature_unit`, `theme`, `default_city_id`, `rain_alert_threshold` | User settings (C/F, dark/light theme) |
| `WeatherAlerts` | `alert_id`, `city_id`, `event_type`, `severity`, `description`, `start_time`, `end_time`, `is_active` | Severe weather warnings and notifications |
| `WeatherHistory` | `record_id`, `city_id`, `temperature`, `humidity`, `wind_speed`, `rainfall_mm`, `recorded_at` | Historical weather logs for analytics |
| `Forecasts` | `forecast_id`, `city_id`, `temperature`, `humidity`, `description`, `wind_speed` | Forecast snapshot cache |

---

## 5. REST Endpoints Handled by Java Servlets

| Endpoint | Method | Class | Description |
|---|---|---|---|
| `/api/weather-data` | `GET` | `WeatherServlet` | 24-hour hourly, 7-day daily forecast & current conditions |
| `/api/city-controller` | `GET` | `CityServlet` | Returns all available and user-saved cities |
| `/api/city-controller` | `POST` | `CityServlet` | Adds new city or toggles favorite status |
| `/api/city-controller` | `DELETE`| `CityServlet` | Removes a city |
| `/api/user-preferences`| `GET` | `PreferenceServlet` | Retrieves user's saved preferences (unit, theme) |
| `/api/user-preferences`| `PUT` | `PreferenceServlet` | Updates user preferences |
| `/api/alert-controller`| `GET` | `AlertServlet` | Returns active alerts or alert history (`?history=true`) |
| `/api/alert-controller`| `POST`| `AlertServlet` | Records a new weather alert |

---

## 6. How to Configure and Run

### Step 1: Configure API Keys
API keys are located in `src/main/webapp/WEB-INF/configAPI.xml`:

```xml
<api-config>
    <openweather-api-key>YOUR_OPENWEATHER_API_KEY</openweather-api-key>
    <google-maps-api-key>YOUR_GOOGLE_MAPS_API_KEY</google-maps-api-key>
</api-config>
```

*(You can also use environment variables `OPENWEATHER_API_KEY` and `GOOGLE_MAPS_API_KEY`)*.

### Step 2: Compile the Java Backend
```bash
mvn clean test-compile
```

### Step 3: Start Jetty Web Server
```bash
mvn org.eclipse.jetty.ee10:jetty-ee10-maven-plugin:12.0.8:run
```
*(Or double-click `run.bat` on Windows)*.

### Step 4: Open in Browser
Visit: **http://localhost:8081/**

- **Dashboard:** `http://localhost:8081/dashboard.html`
- **City Selection (Google Maps):** `http://localhost:8081/select-city.html`
- **Analytics & Charts:** `http://localhost:8081/analytics.html`
- **Weather Alerts:** `http://localhost:8081/alerts.html`
- **User Profile & Preferences:** `http://localhost:8081/user-profile.html`

### Step 5: Packaging for Submission / Production
```bash
mvn clean package
```
Generates standard Jakarta EE10 web archive at: `target/weather-dashboard.war`.
