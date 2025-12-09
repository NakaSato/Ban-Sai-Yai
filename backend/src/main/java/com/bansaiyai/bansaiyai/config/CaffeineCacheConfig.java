package com.bansaiyai.bansaiyai.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine Cache Configuration - Local in-memory cache fallback.
 * 
 * Used when Redis is not available (spring.cache.type=caffeine or default).
 * Provides fast local caching for single-instance deployments.
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "caffeine", matchIfMissing = false)
public class CaffeineCacheConfig {

  @Bean
  public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager();

    cacheManager.setCaffeine(Caffeine.newBuilder()
        .initialCapacity(100)
        .maximumSize(500)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .recordStats());

    // Register cache names
    cacheManager.setCacheNames(java.util.List.of(
        "cashBox",
        "recentTransactions",
        "trialBalance",
        "unclassifiedCount",
        "financialPreviews",
        "parAnalysis",
        "liquidity",
        "membershipTrends",
        "passbook",
        "loanObligation",
        "dividendEstimate",
        "fiscalPeriod",
        "memberSearch",
        "rolePermissions",
        "dashboardStats"));

    return cacheManager;
  }
}
