# core-rule-engine

Simple MVEL-based rule engine for Spring Boot. A lightweight alternative to Drools or Flowable decision tables.

## Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>tech.neatnet</groupId>
    <artifactId>core-rule-engine</artifactId>
    <version>0.1.0</version>
</dependency>
```

### 2. Configure MongoDB

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/rule-engine
```

### 3. Use It

```java
@Autowired
private RuleEngineClient ruleEngineClient;

// Evaluate rules
Map<String, Object> input = Map.of("value", 100, "type", "premium");
List<RuleExecutionResult> results = ruleEngineClient.evaluateRules(
    input,
    Category.PRICING,
    Category.DEFAULT,
    HitPolicy.FIRST
);

// Check results
if (results.get(0).isRuleCriteriaMet()) {
    Map<String, Object> output = results.get(0).getResults();
    // Use output...
}
```

That's it! The library auto-configures everything with sensible defaults.

## Features

- **Decision Tables** - Simple condition-based rules (all conditions must match)
- **Decision Trees** - Binary tree rules with branching logic
- **MVEL Expressions** - Powerful expression language for conditions
- **Caching** - Built-in EhCache for performance
- **Hit Policies** - FIRST (stop on match) or COLLECT (all matches)
- **Spring Boot 3.x** - Native auto-configuration support

## Rule Types

### Decision Table

All conditions must be true (AND logic):

```java
Rule rule = Rule.builder()
    .name("Premium Discount")
    .ruleType(RuleType.DECISION_TABLE)
    .baseRuleCategory(Category.PRICING)
    .baseRuleSubCategory(Category.DEFAULT)
    .conditions(List.of(
        Condition.builder().condition("orderTotal > 100").build(),
        Condition.builder().condition("customerType == 'premium'").build()
    ))
    .results(Map.of("discount", 20, "message", "Premium discount applied"))
    .active(true)
    .build();

ruleEngineClient.saveRule(rule);
```

### Decision Tree

Binary branching with actions at leaf nodes:

```java
Condition tree = Condition.builder()
    .condition("age >= 21")
    .trueBranch(Condition.builder()
        .condition("income > 50000")
        .trueBranch(Condition.builder().action("'APPROVED'").build())
        .falseBranch(Condition.builder().action("'REVIEW'").build())
        .build())
    .falseBranch(Condition.builder().action("'REJECTED'").build())
    .build();

Rule rule = Rule.builder()
    .name("Loan Approval")
    .ruleType(RuleType.DECISION_TREE)
    .baseRuleCategory(Category.WORKFLOW)
    .baseRuleSubCategory(Category.DEFAULT)
    .conditions(List.of(tree))
    .active(true)
    .build();
```

## Configuration

All settings are optional with sensible defaults:

```yaml
rule:
  engine:
    enabled: true                    # Enable/disable (default: true)
    cache:
      enabled: true                  # Enable caching (default: true)
      default-heap-size: 1000        # Rule cache size (default: 1000)
    expression:
      max-cache-size: 10000          # Compiled expression cache (default: 10000)
```

## Performance

The rule engine is optimized for high-throughput scenarios:

### Compiled Expression Caching

MVEL expressions are compiled once and cached for reuse:

```
First evaluation:  ~1-5ms (compile + execute)
Cached evaluation: ~0.01-0.1ms (execute only)
```

The expression cache uses `ConcurrentHashMap` for thread-safe access without locking overhead.

### Rule Caching

Rules are cached by category/subcategory using EhCache:
- Avoids database queries on every evaluation
- Configurable cache size
- Automatic cache invalidation

### Best Practices

1. **Use specific categories** - Narrow category/subcategory filters reduce rules to evaluate
2. **Use FIRST hit policy** - Stops evaluation on first match (faster than COLLECT)
3. **Keep conditions simple** - Complex expressions take longer to evaluate
4. **Pre-warm caches** - Call evaluation once at startup to populate caches

## Custom Categories

Create your own category enums for better organization:

```java
public enum MyCategory implements BaseRuleCategory, BaseRuleSubCategory {
    PRICING, VALIDATION, WORKFLOW, NOTIFICATION;

    @Override
    public String getName() {
        return name();
    }
}
```

Default categories provided: `DEFAULT`, `GENERAL`, `VALIDATION`, `PRICING`, `WORKFLOW`

## Hit Policies

| Policy | Description |
|--------|-------------|
| `FIRST` | Stop on first matching rule |
| `COLLECT` | Evaluate all rules, return all matches |

## Demo Application

See the [examples/rule-engine-demo](examples/rule-engine-demo) for a complete REST API example with:

- Sample rules initialization
- Rule evaluation endpoints
- Custom rule creation
- curl examples for all endpoints

```bash
# Run the demo
cd examples/rule-engine-demo
docker-compose up -d
mvn spring-boot:run

# Initialize sample rules
curl -X POST http://localhost:8080/api/rules/init-samples

# Evaluate a rule
curl -X POST http://localhost:8080/api/rules/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "input": {"customerType": "premium", "orderTotal": 150},
    "category": "PRICING",
    "subCategory": "DEFAULT",
    "hitPolicy": "FIRST"
  }'
```

## Requirements

- Java 17+
- Spring Boot 3.2+
- MongoDB

## License

MIT
