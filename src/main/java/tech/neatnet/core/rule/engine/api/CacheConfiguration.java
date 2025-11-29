package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.ehcache.event.EventType;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.util.List;

/**
 * Auto-configuration for the Rule Engine cache layer.
 * <p>
 * Creates a JCache-based cache manager using EhCache with a default "rules" cache.
 */
@Slf4j
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "rule.engine.cache.enabled", havingValue = "true", matchIfMissing = true)
public class CacheConfiguration {

    private static final String DEFAULT_CACHE_NAME = "rules";

    private final RuleEngineProperties properties;

    public CacheConfiguration(RuleEngineProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public JCacheCacheManager jCacheCacheManager() {
        log.info("Initializing Rule Engine cache manager");
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();

        List<RuleEngineProperties.CacheProperties.CacheConfig> configs =
                properties.getCache().getConfigs();

        if (configs == null || configs.isEmpty()) {
            log.info("No custom cache configs, creating default '{}' cache", DEFAULT_CACHE_NAME);
            createCache(DEFAULT_CACHE_NAME, cacheManager, String.class, Object.class,
                    properties.getCache().getDefaultHeapSize());
        } else {
            log.info("Creating {} configured cache(s)", configs.size());
            configs.forEach(config -> createCache(
                    config.getName(),
                    cacheManager,
                    config.getKeyTypeClass(),
                    config.getValueTypeClass(),
                    config.getHeapSize() != null ? config.getHeapSize()
                            : properties.getCache().getDefaultHeapSize()
            ));
        }

        return new JCacheCacheManager(cacheManager);
    }

    @Bean("customCacheResolver")
    @ConditionalOnMissingBean(name = "customCacheResolver")
    public CacheResolver customCacheResolver(JCacheCacheManager jCacheCacheManager) {
        return new CustomCacheResolver(jCacheCacheManager);
    }

    @Bean("ruleKeyGenerator")
    @ConditionalOnMissingBean(name = "ruleKeyGenerator")
    public KeyGenerator ruleKeyGenerator() {
        return new CacheKeyGenerator();
    }

    private <K, V> void createCache(String cacheName, CacheManager cacheManager,
                                    Class<K> keyType, Class<V> valueType, int heapSize) {
        log.info("Creating cache '{}' [keyType={}, valueType={}, heapSize={}]",
                cacheName, keyType.getSimpleName(), valueType.getSimpleName(), heapSize);

        org.ehcache.config.CacheConfiguration<K, V> cacheConfiguration =
                CacheConfigurationBuilder.newCacheConfigurationBuilder(keyType, valueType,
                                ResourcePoolsBuilder.heap(heapSize))
                        .withService(CacheEventListenerConfigurationBuilder
                                .newEventListenerConfiguration(
                                        new CacheEventLogger<K, V>(),
                                        EventType.CREATED, EventType.UPDATED,
                                        EventType.REMOVED, EventType.EXPIRED)
                                .unordered().asynchronous())
                        .build();

        javax.cache.configuration.Configuration<K, V> jCacheConfiguration =
                Eh107Configuration.fromEhcacheCacheConfiguration(cacheConfiguration);

        cacheManager.createCache(cacheName, jCacheConfiguration);
    }

    static class CacheEventLogger<K, V> implements CacheEventListener<K, V> {
        @Override
        public void onEvent(CacheEvent<? extends K, ? extends V> event) {
            log.debug("Cache event {} for key '{}'", event.getType(), event.getKey());
        }
    }
}
