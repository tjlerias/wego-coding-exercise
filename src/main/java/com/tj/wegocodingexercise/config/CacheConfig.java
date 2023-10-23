package com.tj.wegocodingexercise.config;

import com.tj.wegocodingexercise.dto.CarParkAvailabilityDTO;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import java.util.Map;


@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public org.springframework.cache.CacheManager cacheManager() {
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();

        MutableConfiguration<SimpleKey, Map<String, CarParkAvailabilityDTO>> configuration =
            new MutableConfiguration<SimpleKey, Map<String, CarParkAvailabilityDTO>>()
                .setTypes(SimpleKey.class, (Class<Map<String, CarParkAvailabilityDTO>>) (Class<?>) Map.class)
                .setStoreByValue(false)
                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_MINUTE));

        cacheManager.createCache("carParkAvailability", configuration);

        return new JCacheCacheManager(cacheManager);
    }
}