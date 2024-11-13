package tech.neatnet.core.rule.engine.api;


import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;

import java.util.Collection;
import java.util.Collections;

public class CustomCacheResolver implements CacheResolver {

    private final CacheManager cacheManager;

    public CustomCacheResolver(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        // Determine the cache name dynamically based on the method invocation
        String cacheName = determineCacheName(context);

        // Retrieve the cache from the cache manager
        Cache cache = cacheManager.getCache(cacheName);

        if (cache != null) {
            return Collections.singleton(cache);
        } else {
            return Collections.emptyList();
        }
    }

    private String determineCacheName(CacheOperationInvocationContext<?> context) {
        // Your logic to determine the cache name dynamically based on the method invocation
        // Example: return a hardcoded cache name for demonstration purposes
        return "rules";
    }
}
