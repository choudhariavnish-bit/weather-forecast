package com.weatherapp.db;

import com.weatherapp.config.AppConfig;
import jakarta.servlet.ServletContext;
import java.io.File;
import java.sql.*;

public final class Database {
    private static String dbPath;
    private Database() {}
    public static void init(ServletContext ctx) throws Exception {
        File f = null;
        if (ctx.getRealPath("/") != null) {
            f = new File(ctx.getRealPath("/"), AppConfig.get("DB_FILE"));
        }
        if (f == null || !f.exists()) {
            File fallback = new File(AppConfig.get("DB_FILE"));
            if (fallback.exists()) f = fallback;
            else throw new IllegalStateException("Database file not found: " + (f != null ? f.getAbsolutePath() : AppConfig.get("DB_FILE")));
        }
        dbPath = f.getAbsolutePath();
        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        ensureTables();
    }
    public static Connection getConnection() throws SQLException { return DriverManager.getConnection("jdbc:ucanaccess://" + dbPath + ";memory=false"); }
    private static void ensureTables() throws SQLException {
        try (Connection c=getConnection(); Statement s=c.createStatement()) {
            if (!tableExists(c,"Cities")) s.executeUpdate("CREATE TABLE Cities (CityID AUTOINCREMENT PRIMARY KEY, CityName TEXT(100), CountryCode TEXT(2), Latitude DOUBLE, Longitude DOUBLE, IsActive YESNO)");
            if (!tableExists(c,"Users")) s.executeUpdate("CREATE TABLE Users (UserID AUTOINCREMENT PRIMARY KEY, FullName TEXT(150), Mobile TEXT(20), Email TEXT(150), CityID LONG, PasswordHash TEXT(128), PasswordSalt TEXT(64), IsActive YESNO, CreatedOn DATETIME)");
            if (count(c,"Cities") == 0) {
                insertCity(c,"Aurangabad","IN",19.8762,75.3433);
                insertCity(c,"Jalna","IN",19.8347,75.8816);
                insertCity(c,"Pune","IN",18.5204,73.8567);
                insertCity(c,"Mumbai","IN",19.0760,72.8777);
                insertCity(c,"Delhi","IN",28.6139,77.2090);
            }
        }
    }
    private static boolean tableExists(Connection c,String name) throws SQLException { try(ResultSet r=c.getMetaData().getTables(null,null,name.toUpperCase(),new String[]{"TABLE"})){return r.next();} }
    private static int count(Connection c,String table) throws SQLException { try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT COUNT(*) FROM "+table)){r.next();return r.getInt(1);} }
    private static void insertCity(Connection c,String n,String cc,double lat,double lon)throws SQLException{try(PreparedStatement p=c.prepareStatement("INSERT INTO Cities(CityName,CountryCode,Latitude,Longitude,IsActive) VALUES(?,?,?,?,TRUE)")){p.setString(1,n);p.setString(2,cc);p.setDouble(3,lat);p.setDouble(4,lon);p.executeUpdate();}}
}
