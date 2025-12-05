package com.bansaiyai.bansaiyai.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration for Dashboard KPIs
 * 
 * This configuration is optional and will only be activated if Redis is available.
 * Cache keys follow the format: dashboard:{role}:{userId}:{widget}:{fiscalPeriod}
 * 
 * Default TTL: 5 minutes (as per Requirement 17.4)
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = false)
public class RedisCacheConfig {

    /**
     * Configure Redis Cache Manager with custom TTL settings
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration with 5-minute TTL
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()))
                .disableCachingNullValues();

        // Custom cache configurations for specific widgets
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Officer dashboard caches - shorter TTL for real-time data
        cacheConfigurations.put("cashBox", defaultConfig.entryTtl(Duration.ofMinutes(2)));
        cacheConfigurations.put("recentTransactions", defaultConfig.entryTtl(Duration.ofMinutes(1)));
        
        // Secretary dashboard caches - standard TTL
        cacheConfigurations.put("trialBalance", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("unclassifiedCount", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("financialPreviews", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // President dashboard caches - longer TTL for strategic data
        cacheConfigurations.put("parAnalysis", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("liquidity", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("membershipTrends", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Member dashboard caches - standard TTL
        cacheConfigurations.put("passbook", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("loanObligation", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("dividendEstimate", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // Global caches
        cacheConfigurations.put("fiscalPeriod", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("memberSearch", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
