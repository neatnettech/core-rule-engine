package tech.neatnet.core.rule.engine.api;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.time.Duration;
import java.util.Collection;

import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import static org.ehcache.config.builders.ExpiryPolicyBuilder.timeToLiveExpiration;
import static org.ehcache.config.builders.ResourcePoolsBuilder.newResourcePoolsBuilder;
import static org.ehcache.config.units.MemoryUnit.MB;
import static org.ehcache.jsr107.Eh107Configuration.fromEhcacheCacheConfiguration;

@EnableCaching
@Configuration
class CacheConfiguration {

    private static final String RULES_CACHE = "rules";

    @Bean
    public CacheManager ehCacheManager() {
        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();

        cacheManager.createCache(RULES_CACHE, fromEhcacheCacheConfiguration(
                newCacheConfigurationBuilder(String.class, Collection.class, newResourcePoolsBuilder().offheap(1, MB))
                        .withExpiry(timeToLiveExpiration(Duration.ofSeconds(20)))
        ));

        return cacheManager;
    }
}