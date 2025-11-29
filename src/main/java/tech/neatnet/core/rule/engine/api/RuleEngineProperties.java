package tech.neatnet.core.rule.engine.api;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for the Rule Engine.
 * <p>
 * All properties have sensible defaults for plug-and-play usage.
 * <p>
 * Example (application.yml):
 * <pre>
 * rule:
 *   engine:
 *     enabled: true
 *     cache:
 *       enabled: true
 *       default-heap-size: 1000
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "rule.engine")
public class RuleEngineProperties {

    /**
     * Enable/disable rule engine. Default: true
     */
    private boolean enabled = true;

    /**
     * Cache settings.
     */
    private CacheProperties cache = new CacheProperties();

    /**
     * Expression engine settings.
     */
    private ExpressionProperties expression = new ExpressionProperties();

    @Data
    public static class ExpressionProperties {

        /**
         * Maximum number of compiled expressions to cache.
         * Default: 10000
         */
        private int maxCacheSize = 10_000;
    }

    @Data
    public static class CacheProperties {

        /**
         * Enable/disable caching. Default: true
         */
        private boolean enabled = true;

        /**
         * Default heap size for caches. Default: 1000
         */
        private int defaultHeapSize = 1000;

        /**
         * Custom cache configurations. If empty, default "rules" cache is created.
         */
        private List<CacheConfig> configs = new ArrayList<>();

        @Data
        public static class CacheConfig {

            private String name;
            private String keyType = "java.lang.String";
            private String valueType = "java.lang.Object";
            private Integer heapSize;

            public Class<?> getKeyTypeClass() {
                return resolveClass(keyType, String.class);
            }

            public Class<?> getValueTypeClass() {
                return resolveClass(valueType, Object.class);
            }

            private Class<?> resolveClass(String className, Class<?> defaultClass) {
                if (className == null || className.isBlank()) {
                    return defaultClass;
                }
                try {
                    return Class.forName(className);
                } catch (ClassNotFoundException e) {
                    return defaultClass;
                }
            }
        }
    }
}
