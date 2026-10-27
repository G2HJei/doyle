package xyz.zlatanov.doyle.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static xyz.zlatanov.doyle.adapter.driven.CachingEventOutcomeLookup.EVENT_OUTCOMES_CACHE;

@Configuration
@EnableCaching
public class CachingConfig {

    @Bean
    CacheManager cacheManager() {
        return new TransactionAwareCacheManagerProxy(new ConcurrentMapCacheManager(EVENT_OUTCOMES_CACHE));
    }
}
