package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.ehcache.event.EventType;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

@Slf4j
@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    public JCacheCacheManager jCacheCacheManager() {
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();

        org.ehcache.config.CacheConfiguration<Object, Object> cacheConfiguration =
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class,
                                ResourcePoolsBuilder.heap(100)) // Adjust heap size as needed
                        .withService(CacheEventListenerConfigurationBuilder
                                .newEventListenerConfiguration(new CacheEventLogger(), EventType.CREATED, EventType.UPDATED, EventType.REMOVED, EventType.EXPIRED)
                                .unordered().asynchronous())
                        .build();

        javax.cache.configuration.Configuration<Object, Object> jCacheConfiguration =
                Eh107Configuration.fromEhcacheCacheConfiguration(cacheConfiguration);

        cacheManager.createCache("rules", jCacheConfiguration);

        return new JCacheCacheManager(cacheManager);
    }

    static class CacheEventLogger implements CacheEventListener<Object, Object> {
        @Override
        public void onEvent(CacheEvent<? extends Object, ? extends Object> event) {
            log.info("Cache event {} for item with key {}. Old value = {}, New value = {}",
                    event.getType(), event.getKey(), event.getOldValue(), event.getNewValue());
        }
    }
}
