# Weather Dashboard - HTML5 + Bootstrap 5 + Java

This version adds a common Bootstrap 5 navigation bar and a separate **Select City** page using Google Maps.

## Menu
- Dashboard
- Select City
- User Profile
- Help

## Select City
`select-city.html` provides:
- Google Maps map
- Google Places city search
- Map click selection
- Browser current-location selection
- Reverse geocoding for city/country
- Latitude/longitude display
- Add City to `Cities` table in `ForecastDBMaster.mdb`
- Stored city list

## API Configuration
API keys are stored separately in:

```text
src/main/webapp/WEB-INF/configAPI.xml
```

Edit the file before deployment:

```xml
<api-config>
    <openweather-api-key>YOUR_OPENWEATHER_API_KEY</openweather-api-key>
    <google-maps-api-key>YOUR_GOOGLE_MAPS_API_KEY</google-maps-api-key>
</api-config>
```

`configAPI.xml` is under `WEB-INF`, so it cannot be downloaded directly by a browser. The OpenWeather key is used only by Java on the server. Only the Google Maps browser key is returned by `/api/config`, because Google Maps JavaScript requires the key in the browser. Restrict the Google key by HTTP referrer in Google Cloud Console.

Environment variables with the names `OPENWEATHER_API_KEY` and `GOOGLE_MAPS_API_KEY` can still override the XML values for production deployments.

## Run Locally (Jetty Web Server)
```bash
mvn org.eclipse.jetty.ee10:jetty-ee10-maven-plugin:12.0.8:run
```
Open your browser at: **http://localhost:8080/**

## Build WAR (Production Deployment)
```bash
mvn clean package
```
Deploy `target/weather-dashboard.war` to Apache Tomcat 10.1+ or Jetty 12.

## Public Index / About
The public landing page is `index.html`. It contains an India-focused globe illustration, Register/Login links, an overview of Indian weather, and project participant placeholders. `about.html` provides the expanded Indian weather section and participant list.

Replace the participant placeholders in `index.html` and `about.html` with the actual project participant names.
