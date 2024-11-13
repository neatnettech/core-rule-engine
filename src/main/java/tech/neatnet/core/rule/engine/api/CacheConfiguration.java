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

    private final CacheConfigProperties cacheConfigProperties;

    public CacheConfiguration(CacheConfigProperties cacheConfigProperties) {
        this.cacheConfigProperties = cacheConfigProperties;
    }

    @Bean
    public JCacheCacheManager jCacheCacheManager() {
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();

        cacheConfigProperties.getCacheConfigs().forEach(
                cacheConfig -> createCache(cacheConfig.getName(), cacheManager, cacheConfig.getKeyType(), cacheConfig.getValueType(), cacheConfig.getHeapSize())
        );

//        createCache("rules", cacheManager, Object.class, Object.class, 100);
//        createCache("anothcOerCache", cacheManager, String.class, Integer.class, 50);

        return new JCacheCacheManager(cacheManager);
    }

    private <K, V> void createCache(String cacheName, CacheManager cacheManager,
                                    Class<K> keyType, Class<V> valueType, int heapSize) {
        org.ehcache.config.CacheConfiguration<K, V> cacheConfiguration =
                CacheConfigurationBuilder.newCacheConfigurationBuilder(keyType, valueType,
                                ResourcePoolsBuilder.heap(heapSize))
                        .withService(CacheEventListenerConfigurationBuilder
                                .newEventListenerConfiguration(new CacheEventLogger<K, V>(), EventType.CREATED, EventType.UPDATED, EventType.REMOVED, EventType.EXPIRED)
                                .unordered().asynchronous())
                        .build();

        javax.cache.configuration.Configuration<K, V> jCacheConfiguration =
                Eh107Configuration.fromEhcacheCacheConfiguration(cacheConfiguration);

        cacheManager.createCache(cacheName, jCacheConfiguration);
    }

    static class CacheEventLogger<K, V> implements CacheEventListener<K, V> {
        @Override
        public void onEvent(CacheEvent<? extends K, ? extends V> event) {
            log.info("Cache event {} for item with key {}. Old value = {}, New value = {}",
                    event.getType(), event.getKey(), event.getOldValue(), event.getNewValue());
        }
    }
}
