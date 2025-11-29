package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core MVEL expression evaluation engine with compiled expression caching.
 * <p>
 * Performance optimizations:
 * <ul>
 *     <li>Compiled expressions are cached in a ConcurrentHashMap</li>
 *     <li>Thread-safe expression evaluation</li>
 *     <li>Lazy compilation on first use</li>
 * </ul>
 */
@Slf4j
class CoreRuleEngine {

    /**
     * Cache for compiled MVEL expressions.
     * Key: expression string, Value: compiled expression
     */
    private final ConcurrentHashMap<String, Serializable> expressionCache = new ConcurrentHashMap<>();

    /**
     * Shared parser context for consistent compilation settings.
     */
    private final ParserContext parserContext;

    /**
     * Maximum cache size to prevent memory issues (default: 10,000 expressions)
     */
    private final int maxCacheSize;

    public CoreRuleEngine() {
        this(10_000);
    }

    public CoreRuleEngine(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
        this.parserContext = createParserContext();
        log.info("CoreRuleEngine initialized with expression cache (maxSize={})", maxCacheSize);
    }

    /**
     * Evaluates a boolean condition against the provided data.
     *
     * @param condition MVEL expression that returns a boolean
     * @param data      variables available to the expression
     * @return result of condition evaluation
     */
    public boolean evaluateCondition(String condition, Map<String, Object> data) {
        if (condition == null || condition.isBlank()) {
            log.debug("Empty condition, returning true");
            return true;
        }

        log.debug("Evaluating condition: {} with data: {}", condition, data);
        Serializable compiledExpression = getCompiledExpression(condition);
        Object result = MVEL.executeExpression(compiledExpression, data);

        boolean boolResult = result instanceof Boolean ? (Boolean) result : Boolean.parseBoolean(String.valueOf(result));
        log.debug("Condition result: {}", boolResult);
        return boolResult;
    }

    /**
     * Executes an action expression and returns the result.
     *
     * @param action MVEL expression to execute
     * @param data   variables available to the expression
     * @return result of action execution
     */
    public Optional<Object> executeAction(String action, Map<String, Object> data) {
        if (action == null || action.isBlank()) {
            log.debug("Empty action, returning empty");
            return Optional.empty();
        }

        log.debug("Executing action: {} with data: {}", action, data);
        Serializable compiledExpression = getCompiledExpression(action);
        Object result = MVEL.executeExpression(compiledExpression, data);
        log.debug("Action result: {}", result);
        return Optional.ofNullable(result);
    }

    /**
     * Evaluates a condition with optional inValues parameter.
     *
     * @param condition MVEL expression
     * @param data      variables available to the expression
     * @param inValues  optional array of values to include as "inValues" variable
     * @return result of evaluation
     */
    public boolean evaluate(String condition, Map<String, Object> data, Optional<String[]> inValues) {
        inValues.ifPresent(values -> data.put("inValues", values));
        return evaluateCondition(condition, data);
    }

    /**
     * Gets a compiled expression from cache, or compiles and caches it.
     *
     * @param expression MVEL expression string
     * @return compiled expression
     */
    private Serializable getCompiledExpression(String expression) {
        return expressionCache.computeIfAbsent(expression, this::compileExpression);
    }

    /**
     * Compiles an MVEL expression.
     */
    private Serializable compileExpression(String expression) {
        // Evict oldest entries if cache is full (simple strategy)
        if (expressionCache.size() >= maxCacheSize) {
            log.warn("Expression cache full (size={}), clearing cache", maxCacheSize);
            expressionCache.clear();
        }

        log.debug("Compiling expression: {}", expression);
        return MVEL.compileExpression(expression, parserContext);
    }

    /**
     * Creates a shared parser context with common settings.
     */
    private ParserContext createParserContext() {
        ParserContext ctx = new ParserContext();
        // Add common imports if needed
        // ctx.addImport("Math", Math.class);
        // ctx.addImport("String", String.class);
        return ctx;
    }

    /**
     * Pre-compiles expressions for a set of rules.
     * Call this during application startup for better first-request performance.
     *
     * @param expressions expressions to pre-compile
     */
    public void preCompileExpressions(Iterable<String> expressions) {
        log.info("Pre-compiling expressions...");
        int count = 0;
        for (String expression : expressions) {
            if (expression != null && !expression.isBlank()) {
                getCompiledExpression(expression);
                count++;
            }
        }
        log.info("Pre-compiled {} expressions", count);
    }

    /**
     * Clears the expression cache.
     */
    public void clearCache() {
        log.info("Clearing expression cache (size={})", expressionCache.size());
        expressionCache.clear();
    }

    /**
     * Returns current cache statistics.
     */
    public CacheStats getCacheStats() {
        return new CacheStats(expressionCache.size(), maxCacheSize);
    }

    public record CacheStats(int size, int maxSize) {
        public double utilizationPercent() {
            return maxSize > 0 ? (size * 100.0 / maxSize) : 0;
        }
    }
}
