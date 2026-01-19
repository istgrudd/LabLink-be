package com.mbclab.lablink.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Konfigurasi caching untuk resource optimization.
 * Menggunakan ConcurrentMapCacheManager (in-memory) untuk simple caching.
 * Untuk production, ganti dengan Redis/Caffeine.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String ACTIVE_PERIOD_CACHE = "activePeriod";
    public static final String ALL_PERIODS_CACHE = "allPeriods";
    
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                ACTIVE_PERIOD_CACHE,
                ALL_PERIODS_CACHE
        );
    }
}
