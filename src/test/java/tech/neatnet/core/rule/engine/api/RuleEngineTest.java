package tech.neatnet.core.rule.engine.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.neatnet.core.rule.engine.domain.Rule;
import tech.neatnet.core.rule.engine.domain.RuleExecutionResult;
import tech.neatnet.core.rule.engine.domain.RuleMatrix;

@ExtendWith(MockitoExtension.class)
class RuleEngineTest {

  @Mock
  private CoreRuleEngine coreRuleEngine;

  @Mock
  private RuleMatrixCache ruleMatrixCache;

  @Mock
  private RuleMatrixService ruleMatrixService;

  private RuleEngine ruleEngine;

  @BeforeEach
  void setUp() {
    ruleEngine = new RuleEngine(coreRuleEngine, ruleMatrixCache, ruleMatrixService);
  }

  @Test
  @DisplayName("Test evaluateAllRules with no rules")
  void testEvaluateAllRulesWithNoRules() {
    when(ruleMatrixCache.getAllRuleMatrices()).thenReturn(Collections.emptyList());

    Optional<List<RuleExecutionResult>> results = ruleEngine.evaluateAllRules(new HashMap<>());

    assertTrue(results.isPresent());
    assertTrue(results.get().isEmpty());
  }


  @Test
  @DisplayName("Test evaluateAllRules with true condition")
  void testEvaluateAllRulesWithTrueCondition() {
    // Create a rule as per the test requirements
    Rule rule = Rule.builder()
        .condition("value1 == 'ABC'")
        .build();

    // Define a non-empty resultMap for the RuleMatrix
    Map<String, Object> resultMap = Map.of("someKey", "someValue");

    // Create a RuleMatrix and add the rule and resultMap to it
    RuleMatrix matrix = RuleMatrix.builder()
        .id(UUID.randomUUID())
        .rules(Collections.singletonList(rule))
        .results(resultMap)
        .build();

    // Mocking behavior of dependencies
    when(ruleMatrixCache.getAllRuleMatrices()).thenReturn(Collections.singletonList(matrix));
    when(coreRuleEngine.evaluateCondition(anyString(), anyMap(), any())).thenReturn(true);

    // Evaluate all rules
    Optional<List<RuleExecutionResult>> results = ruleEngine.evaluateAllRules(new HashMap<>());

    // Assertions
    assertTrue(results.isPresent());
    assertFalse(results.get().isEmpty());
    assertEquals(1, results.get().size());
    assertFalse(results.get().get(0).getResults().isEmpty());
    assertEquals(resultMap, results.get().get(0).getResults());
  }


  @Test
  @DisplayName("Test evaluateAllRules with false condition")
  void testEvaluateAllRulesWithFalseCondition() {
    Rule rule = Rule.builder().condition("condition").build();
    RuleMatrix matrix = RuleMatrix.builder()
        .id(UUID.randomUUID())
        .rules(Collections.singletonList(rule))
        .results(Map.of("key", "value")) // Assuming this is the desired result map
        .build();

    when(ruleMatrixCache.getAllRuleMatrices()).thenReturn(Collections.singletonList(matrix));
    when(coreRuleEngine.evaluateCondition(anyString(), anyMap(), any())).thenReturn(false);

    Optional<List<RuleExecutionResult>> results = ruleEngine.evaluateAllRules(new HashMap<>());

    assertTrue(results.isPresent());
    assertEquals(1, results.get().size());
    assertTrue(results.get().get(0).getResults().isEmpty());
  }


  @Test
  @DisplayName("Test evaluateAllRules with complex inputVariables")
  void testEvaluateAllRulesWithComplexInputVariables() {
    Rule rule = Rule.builder().condition("value1 == 'ABC'").build();
    Rule rule1 = Rule.builder().condition("value2 > 100").build();
    Rule rule2 = Rule.builder().condition("value3 in inValues").inValues(List.of("1", "2", "3"))
        .build();
    RuleMatrix matrix = RuleMatrix.builder()
        .id(UUID.randomUUID())
        .rules(List.of(rule, rule1, rule2))
        .results(Map.of("hit", true)) // Assuming this is the desired result map
        .build();

    when(ruleMatrixCache.getAllRuleMatrices()).thenReturn(Collections.singletonList(matrix));
    when(coreRuleEngine.evaluateCondition(anyString(), anyMap(), any())).thenReturn(true);

    Map<String, Object> inputVariables = new HashMap<>();
    inputVariables.put("value1", "ABC");
    inputVariables.put("value2", 101);
    inputVariables.put("value3", "2");

    Optional<List<RuleExecutionResult>> results = ruleEngine.evaluateAllRules(inputVariables);

    assertTrue(results.isPresent());
    assertFalse(results.get().isEmpty());
    assertEquals(Map.of("hit", true), results.get().get(0).getResults());
  }


  @Test
  @DisplayName("Test evaluateAllRules with hit result assigned on Matrix level")
  void testEvaluateAllRulesWithHitResult() {
    // Create rules as before
    Rule rule = Rule.builder().condition("value1 == 'ABC'").build();
    Rule rule1 = Rule.builder().condition("value2 > 100").build();
    Rule rule2 = Rule.builder().condition("value3 in inValues").inValues(List.of("1", "2", "3"))
        .build();

    // Define the resultMap for the RuleMatrix
    Map<String, Object> resultMap = Map.of("hit", true);

    // Set up RuleMatrix with the rules and predefined results
    RuleMatrix matrix = RuleMatrix.builder()
        .id(UUID.randomUUID())
        .rules(Arrays.asList(rule, rule1, rule2))
        .results(resultMap)
        .build();

    // Mocking behavior of dependencies
    when(ruleMatrixCache.getAllRuleMatrices()).thenReturn(Collections.singletonList(matrix));
    when(coreRuleEngine.evaluateCondition(anyString(), anyMap(), any())).thenReturn(true);

    // Define input variables
    Map<String, Object> inputVariables = new HashMap<>();
    inputVariables.put("value1", "ABC");
    inputVariables.put("value2", 101);
    inputVariables.put("value3", "2");

    // Evaluate all rules
    Optional<List<RuleExecutionResult>> optionalResults = ruleEngine.evaluateAllRules(
        inputVariables);

    // Assertions
    assertTrue(optionalResults.isPresent());
    List<RuleExecutionResult> results = optionalResults.get();
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(resultMap, results.get(0).getResults());
  }


}
