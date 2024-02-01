package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import java.time.Duration;
import java.util.Collection;

import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import static org.ehcache.config.builders.ExpiryPolicyBuilder.timeToLiveExpiration;
import static org.ehcache.config.builders.ResourcePoolsBuilder.newResourcePoolsBuilder;
import static org.ehcache.config.units.MemoryUnit.MB;
import static org.ehcache.jsr107.Eh107Configuration.fromEhcacheCacheConfiguration;

@Slf4j
@EnableCaching
@Configuration
class CacheConfiguration {

    @Value("${cache.rules.name}")
    private String rulesCacheName;

    @Bean
    public CacheManager ehCacheManager() {
        log.debug("Initializing EhCacheManager with rules cache name: {}", rulesCacheName);
        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();

        cacheManager.createCache(rulesCacheName, fromEhcacheCacheConfiguration(
                newCacheConfigurationBuilder(String.class, Collection.class, newResourcePoolsBuilder().offheap(1, MB))
                        .withExpiry(timeToLiveExpiration(Duration.ofSeconds(20)))
        ));
        log.debug("EhCacheManager initialized");
        return cacheManager;
    }
}