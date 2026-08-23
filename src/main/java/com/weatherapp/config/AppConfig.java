package com.weatherapp.config;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import jakarta.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.Properties;

/**
 * Application configuration.
 *
 * API keys are read from /WEB-INF/configAPI.xml.
 * Environment variables remain supported as an optional deployment override.
 */
public final class AppConfig {
    private static final Properties P = new Properties();
    private static volatile Document apiConfig;

    static {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) P.load(in);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private AppConfig() {}

    /** Load /WEB-INF/configAPI.xml once using the servlet context. */
    public static synchronized void initialize(ServletContext context) {
        if (apiConfig != null) return;
        try (InputStream in = context.getResourceAsStream("/WEB-INF/configAPI.xml")) {
            if (in == null) {
                throw new IllegalStateException("Missing /WEB-INF/configAPI.xml");
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            apiConfig = factory.newDocumentBuilder().parse(in);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load /WEB-INF/configAPI.xml: " + e.getMessage(), e);
        }
    }

    public static String get(String key) {
        // Environment variables can override configuration for production deployments.
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) return env.trim();

        if ("OPENWEATHER_API_KEY".equals(key)) return xmlValue("openweather-api-key");
        if ("GOOGLE_MAPS_API_KEY".equals(key)) return xmlValue("google-maps-api-key");
        return P.getProperty(key, "").trim();
    }

    private static String xmlValue(String tag) {
        if (apiConfig == null) {
            throw new IllegalStateException("AppConfig is not initialized. Call AppConfig.initialize(servletContext) first.");
        }
        NodeList nodes = apiConfig.getElementsByTagName(tag);
        if (nodes.getLength() == 0) return "";
        return nodes.item(0).getTextContent().trim();
    }
}
