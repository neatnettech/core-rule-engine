package tech.neatnet.core.rule.engine.api;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom cache resolver for the Rule Engine.
 * <p>
 * Resolves cache names dynamically based on method invocation context.
 * Falls back to the default "rules" cache.
 */
public class CustomCacheResolver implements CacheResolver {

    private static final String DEFAULT_CACHE_NAME = "rules";

    private final CacheManager cacheManager;

    public CustomCacheResolver(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        String cacheName = determineCacheName(context);
        Cache cache = cacheManager.getCache(cacheName);

        if (cache != null) {
            return Collections.singleton(cache);
        }

        // Fall back to default cache
        Cache defaultCache = cacheManager.getCache(DEFAULT_CACHE_NAME);
        if (defaultCache != null) {
            return Collections.singleton(defaultCache);
        }

        return Collections.emptyList();
    }

    /**
     * Determines the cache name based on the method invocation context.
     * <p>
     * Override this method to implement custom cache name resolution logic.
     *
     * @param context the cache operation invocation context
     * @return the cache name to use
     */
    protected String determineCacheName(CacheOperationInvocationContext<?> context) {
        // Default implementation returns the standard "rules" cache
        // Can be extended to support per-category caches, etc.
        return DEFAULT_CACHE_NAME;
    }
}
