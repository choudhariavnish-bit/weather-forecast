package com.weather.service;

import com.weather.model.WeatherData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory thread-safe cache service for weather forecast data.
 * Caches WeatherData objects by location key with an eviction TTL (default 10 minutes)
 * to respect OpenWeather API limits and ensure ultra-fast response times.
 */
public class CacheService {
    private static final long DEFAULT_TTL_MS = 10 * 60 * 1000L; // 10 minutes

    private static class CacheEntry {
        final WeatherData data;
        final long expiresAt;

        CacheEntry(WeatherData data, long ttlMs) {
            this.data = data;
            this.expiresAt = System.currentTimeMillis() + ttlMs;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public WeatherData get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null) {
            if (!entry.isExpired()) {
                return entry.data;
            }
            cache.remove(key);
        }
        return null;
    }

    public void put(String key, WeatherData data) {
        put(key, data, DEFAULT_TTL_MS);
    }

    public void put(String key, WeatherData data, long ttlMs) {
        cache.put(key, new CacheEntry(data, ttlMs));
    }

    public void invalidate(String key) {
        cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }
}
