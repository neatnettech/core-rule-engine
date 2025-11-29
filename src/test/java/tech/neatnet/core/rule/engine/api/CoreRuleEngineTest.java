package tech.neatnet.core.rule.engine.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CoreRuleEngine Tests")
class CoreRuleEngineTest {

    private CoreRuleEngine coreRuleEngine;

    @BeforeEach
    void setUp() {
        coreRuleEngine = new CoreRuleEngine(100);
    }

    @Nested
    @DisplayName("evaluateCondition")
    class EvaluateCondition {

        @Test
        @DisplayName("should return true for simple true condition")
        void shouldReturnTrueForSimpleTrueCondition() {
            Map<String, Object> data = Map.of("value", 10);
            assertTrue(coreRuleEngine.evaluateCondition("value > 5", data));
        }

        @Test
        @DisplayName("should return false for simple false condition")
        void shouldReturnFalseForSimpleFalseCondition() {
            Map<String, Object> data = Map.of("value", 3);
            assertFalse(coreRuleEngine.evaluateCondition("value > 5", data));
        }

        @Test
        @DisplayName("should handle string comparison")
        void shouldHandleStringComparison() {
            Map<String, Object> data = Map.of("type", "premium");
            assertTrue(coreRuleEngine.evaluateCondition("type == 'premium'", data));
        }

        @Test
        @DisplayName("should handle complex boolean expressions")
        void shouldHandleComplexBooleanExpressions() {
            Map<String, Object> data = Map.of("age", 25, "income", 60000);
            assertTrue(coreRuleEngine.evaluateCondition("age >= 21 && income > 50000", data));
        }

        @Test
        @DisplayName("should return true for null or blank condition")
        void shouldReturnTrueForNullOrBlankCondition() {
            Map<String, Object> data = Map.of("value", 10);
            assertTrue(coreRuleEngine.evaluateCondition(null, data));
            assertTrue(coreRuleEngine.evaluateCondition("", data));
            assertTrue(coreRuleEngine.evaluateCondition("  ", data));
        }

        @Test
        @DisplayName("should handle contains check")
        void shouldHandleContainsCheck() {
            Map<String, Object> data = Map.of("email", "user@example.com");
            assertTrue(coreRuleEngine.evaluateCondition("email.contains('@')", data));
        }
    }

    @Nested
    @DisplayName("executeAction")
    class ExecuteAction {

        @Test
        @DisplayName("should return string result")
        void shouldReturnStringResult() {
            Map<String, Object> data = new HashMap<>();
            Optional<Object> result = coreRuleEngine.executeAction("'APPROVED'", data);
            assertTrue(result.isPresent());
            assertEquals("APPROVED", result.get());
        }

        @Test
        @DisplayName("should return computed result")
        void shouldReturnComputedResult() {
            Map<String, Object> data = Map.of("price", 100, "discount", 20);
            Optional<Object> result = coreRuleEngine.executeAction("price - discount", data);
            assertTrue(result.isPresent());
            assertEquals(80, result.get());
        }

        @Test
        @DisplayName("should return empty for null or blank action")
        void shouldReturnEmptyForNullOrBlankAction() {
            Map<String, Object> data = new HashMap<>();
            assertTrue(coreRuleEngine.executeAction(null, data).isEmpty());
            assertTrue(coreRuleEngine.executeAction("", data).isEmpty());
        }
    }

    @Nested
    @DisplayName("Expression Caching")
    class ExpressionCaching {

        @Test
        @DisplayName("should cache compiled expressions")
        void shouldCacheCompiledExpressions() {
            Map<String, Object> data = Map.of("value", 10);
            String expression = "value > 5";

            // First call compiles
            coreRuleEngine.evaluateCondition(expression, data);
            CoreRuleEngine.CacheStats stats1 = coreRuleEngine.getCacheStats();
            assertEquals(1, stats1.size());

            // Second call uses cache
            coreRuleEngine.evaluateCondition(expression, data);
            CoreRuleEngine.CacheStats stats2 = coreRuleEngine.getCacheStats();
            assertEquals(1, stats2.size()); // Still 1, not 2
        }

        @Test
        @DisplayName("should clear cache")
        void shouldClearCache() {
            Map<String, Object> data = Map.of("value", 10);
            coreRuleEngine.evaluateCondition("value > 5", data);
            coreRuleEngine.evaluateCondition("value < 20", data);

            assertEquals(2, coreRuleEngine.getCacheStats().size());

            coreRuleEngine.clearCache();

            assertEquals(0, coreRuleEngine.getCacheStats().size());
        }

        @Test
        @DisplayName("should report cache utilization")
        void shouldReportCacheUtilization() {
            CoreRuleEngine smallCache = new CoreRuleEngine(10);
            Map<String, Object> data = Map.of("value", 10);

            for (int i = 0; i < 5; i++) {
                smallCache.evaluateCondition("value > " + i, data);
            }

            CoreRuleEngine.CacheStats stats = smallCache.getCacheStats();
            assertEquals(5, stats.size());
            assertEquals(10, stats.maxSize());
            assertEquals(50.0, stats.utilizationPercent(), 0.01);
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("should throw exception for invalid expression")
        void shouldThrowExceptionForInvalidExpression() {
            Map<String, Object> data = Map.of("value", 10);
            assertThrows(Exception.class, () ->
                    coreRuleEngine.evaluateCondition("invalid syntax {{{}}", data));
        }

        @Test
        @DisplayName("should throw exception for missing variable")
        void shouldThrowExceptionForMissingVariable() {
            Map<String, Object> data = new HashMap<>();
            assertThrows(Exception.class, () ->
                    coreRuleEngine.evaluateCondition("missingVar > 5", data));
        }
    }
}
