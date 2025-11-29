package tech.neatnet.core.rule.engine.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RuleEngineProperties Tests")
class RuleEnginePropertiesTest {

    @Test
    @DisplayName("should have sensible defaults")
    void shouldHaveSensibleDefaults() {
        RuleEngineProperties properties = new RuleEngineProperties();

        assertTrue(properties.isEnabled());
        assertNotNull(properties.getCache());
        assertTrue(properties.getCache().isEnabled());
        assertEquals(1000, properties.getCache().getDefaultHeapSize());
        assertNotNull(properties.getExpression());
        assertEquals(10_000, properties.getExpression().getMaxCacheSize());
    }

    @Test
    @DisplayName("CacheConfig should resolve class types correctly")
    void cacheConfigShouldResolveClassTypes() {
        RuleEngineProperties.CacheProperties.CacheConfig config =
                new RuleEngineProperties.CacheProperties.CacheConfig();

        config.setKeyType("java.lang.String");
        config.setValueType("java.lang.Integer");

        assertEquals(String.class, config.getKeyTypeClass());
        assertEquals(Integer.class, config.getValueTypeClass());
    }

    @Test
    @DisplayName("CacheConfig should use defaults for invalid class names")
    void cacheConfigShouldUseDefaultsForInvalidClassNames() {
        RuleEngineProperties.CacheProperties.CacheConfig config =
                new RuleEngineProperties.CacheProperties.CacheConfig();

        config.setKeyType("invalid.class.Name");
        config.setValueType(null);

        assertEquals(String.class, config.getKeyTypeClass());
        assertEquals(Object.class, config.getValueTypeClass());
    }
}
