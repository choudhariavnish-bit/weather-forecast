package com.weather.config;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import jakarta.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {
    private static final Properties P = new Properties();
    private static volatile Document apiConfig;

    static {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) P.load(in);
        } catch (Exception ignored) {}
    }

    private AppConfig() {}

    public static synchronized void initialize(ServletContext context) {
        if (apiConfig != null) return;
        try (InputStream in = context.getResourceAsStream("/WEB-INF/configAPI.xml")) {
            if (in == null) return;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            apiConfig = factory.newDocumentBuilder().parse(in);
        } catch (Exception ignored) {}
    }

    public static String get(String key) {
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) return env.trim();

        if ("OPENWEATHER_API_KEY".equals(key)) return xmlValue("openweather-api-key");
        if ("GOOGLE_MAPS_API_KEY".equals(key)) return xmlValue("google-maps-api-key");
        return P.getProperty(key, "").trim();
    }

    private static String xmlValue(String tag) {
        if (apiConfig == null) {
            loadFileFallback();
        }
        if (apiConfig == null) return "";
        NodeList nodes = apiConfig.getElementsByTagName(tag);
        if (nodes.getLength() == 0) return "";
        return nodes.item(0).getTextContent().trim();
    }

    private static synchronized void loadFileFallback() {
        if (apiConfig != null) return;
        java.io.File[] candidates = new java.io.File[] {
            new java.io.File("src/main/webapp/WEB-INF/configAPI.xml"),
            new java.io.File("WEB-INF/configAPI.xml"),
            new java.io.File("../src/main/webapp/WEB-INF/configAPI.xml")
        };
        for (java.io.File f : candidates) {
            if (f.exists()) {
                try (java.io.InputStream in = new java.io.FileInputStream(f)) {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                    factory.setXIncludeAware(false);
                    factory.setExpandEntityReferences(false);
                    apiConfig = factory.newDocumentBuilder().parse(in);
                    return;
                } catch (Exception ignored) {}
            }
        }
    }
}
