package tech.neatnet.core.rule.engine.api;

import java.time.Duration;
import java.util.Collection;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@EnableCaching
@Configuration
class CacheConfiguration {

  private static final String RULES_CACHE = "rules";

  @Bean
  public CacheManager ehCacheManager() {
    CachingProvider provider = Caching.getCachingProvider();
    CacheManager cacheManager = provider.getCacheManager();

    CacheConfigurationBuilder<String, Collection> configuration =
        CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String.class,
                Collection.class,
                ResourcePoolsBuilder
                    .newResourcePoolsBuilder().offheap(1, MemoryUnit.MB))
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(20)));

    javax.cache.configuration.Configuration<String, Collection> stringDoubleConfiguration =
        Eh107Configuration.fromEhcacheCacheConfiguration(configuration);

    cacheManager.createCache(RULES_CACHE, stringDoubleConfiguration);
    return cacheManager;

  }
}
