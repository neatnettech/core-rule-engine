# core-rule-engine

## Description
Rule engine design based on MVEL and Spring.

## Features
* Rule execution based on MVEL expression stored in database and cache

### IMPORTANT
This library is alpha version and is not ready for production use.


## Usage
* Add dependency
```
    <dependency>
      <artifactId>core-rule-engine</artifactId>
      <groupId>tech.neatnet</groupId>
      <version>${core.rule.engine.version}</version>
    </dependency>
```

* Add configuration (Autorired)
```
    @Autowired
    private RuleEngine ruleEngine;
```

* Add sample rule through RuleController
```
    @Autowired
    private RuleController ruleController;

    @GetMapping("/addRule")
    public void testAddRule() {
        try {
            RuleMatrix rm = ruleController.saveRuleMatrix();
        } catch (Exception e) {
            log.error("Error creating RuleMatrix", e);
        }
        return HttpStatus.OK.toString();
    }
```

* Implement rule evaluation
```
    Optional<List<RuleExecutionResult>> ruleExecutionResults = ruleEngine.evaluateAllRules(
        signalRequest.getValues());
```

* Try it out
```
POST http://localhost:8080/signal
Content-Type: application/json

{
  "values": {
    "value": 11,
    "value2": "test",
    "value3": "test1"
  }
}
```

