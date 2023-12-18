package tech.neatnet.core.rule.engine.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.neatnet.core.rule.engine.domain.Condition;
import tech.neatnet.core.rule.engine.domain.Rule;
import tech.neatnet.core.rule.engine.domain.RuleExecutionResult;
import tech.neatnet.core.rule.engine.domain.TreeExecutionResult;

@ExtendWith(MockitoExtension.class)
class RuleEngineTest {

  @Mock
  private CoreRuleEngine coreRuleEngine;

  @Mock
  private RuleCache ruleCache;

  private RuleEngine ruleEngine;
  private Rule matrixRuleValueEqual;
  private Rule matrixRuleValueGreater;
  private Rule matrixRuleMultipleConditions;
  private Rule decisionTreeRuleValueEqual;

  @BeforeEach
  public void setUp() {
    matrixRuleValueEqual = Rule.builder()
        .conditions(Collections.singletonList(
            Condition.builder()
                .condition("value1 == 'ABC'")
                .build()))
        .build();

    matrixRuleValueGreater = Rule.builder()
        .conditions(Collections.singletonList(
            Condition.builder()
                .condition("value2 > 100")
                .build()))
        .build();
    matrixRuleMultipleConditions = Rule.builder()
        .conditions(List.of(Condition.builder()
                    .condition("value1 == 'ABC'")
                    .build()
                ,
                Condition.builder()
                    .condition("value2 > 100")
                    .build()
                ,
                Condition.builder()
                    .condition("value3 in inValues")
                    .inValues(List.of("1", "2", "3"))
                    .build()
            )

        )
        .build();

    decisionTreeRuleValueEqual = Rule.builder()
        .conditions(Collections.singletonList(
            Condition.builder()
                .condition("value1 == 'ABC'")
                .trueBranch(
                    Condition.builder()
                        .condition("value2 > 100")
                        .action("result = 'success'")
                        .build())
                .falseBranch(
                    Condition.builder()
                        .condition("value2 < 100")
                        .action("result = 'success'")
                        .build())
                .build()))
        .build();

    // When ruleCache.getAllRules() is called, return the test rules
    when(ruleCache.getAllRules()).thenReturn(
        Arrays.asList(matrixRuleValueEqual, matrixRuleValueGreater, matrixRuleMultipleConditions));
    when(ruleCache.getAllDecisionTrees()).thenReturn(
        Collections.singletonList(decisionTreeRuleValueEqual));

    ruleEngine = new RuleEngine(coreRuleEngine, ruleCache);
  }

  @Test
  @DisplayName("Test evaluateAllRules with no rules")
  void testEvaluateAllRulesWithNoRules() {
    Optional<List<RuleExecutionResult>> results = ruleEngine.evaluateRules(new HashMap<>());

    assertTrue(results.isPresent());
    assertFalse(results.get().isEmpty());
  }


  @Test
  @DisplayName("Test evaluateAllRules with true condition")
  void testEvaluateAllRulesWithTrueCondition() {

    // Define a non-empty resultMap for the RuleMatrix
    Map<String, Object> resultMap = Map.of("someKey", "someValue");

    // Mocking behavior of dependencies
    when(coreRuleEngine.evaluateCondition(anyString(), anyMap(), any())).thenReturn(true);

    // Evaluate all rules
    Optional<List<RuleExecutionResult>> results = ruleEngine.evaluateRules(new HashMap<>());

    // Assertions
    assertTrue(results.isPresent());
    assertFalse(results.get().isEmpty());
    assertEquals(3, results.get().size());
    assertNull(results.get().get(0).getResults());
  }


  @Test
  @DisplayName("Test evaluateAllRules with false condition")
  void testEvaluateAllRulesWithFalseCondition() {

    when(coreRuleEngine.evaluateCondition(anyString(), anyMap(), any())).thenReturn(false);

    Optional<List<RuleExecutionResult>> results = ruleEngine.evaluateRules(new HashMap<>());

    assertTrue(results.isPresent());
    assertEquals(3, results.get().size());
    assertTrue(results.get().get(0).getResults().isEmpty());
  }


  @Test
  @DisplayName("Test evaluateAllRules with complex inputVariables")
  void testEvaluateAllRulesWithComplexInputVariables() {

    when(coreRuleEngine.evaluateCondition(anyString(), anyMap(), any())).thenReturn(true);

    Map<String, Object> inputVariables = new HashMap<>();
    inputVariables.put("value1", "ABC");
    inputVariables.put("value2", 101);
    inputVariables.put("value3", "2");

    Optional<List<RuleExecutionResult>> results = ruleEngine.evaluateRules(inputVariables);

    assertTrue(results.isPresent());
    assertFalse(results.get().isEmpty());
  }


  @Test
  @DisplayName("Test evaluateAllRules with hit result assigned on Matrix level")
  void testEvaluateAllRulesWithHitResult() {

    when(coreRuleEngine.evaluateCondition(anyString(), anyMap(), any())).thenReturn(true);

    // Define input variables
    Map<String, Object> inputVariables = new HashMap<>();
    inputVariables.put("value1", "ABC");
    inputVariables.put("value2", 101);
    inputVariables.put("value3", "2");

    // Evaluate all rules
    Optional<List<RuleExecutionResult>> optionalResults = ruleEngine.evaluateRules(
        inputVariables);

    // Assertions
    assertTrue(optionalResults.isPresent());
    List<RuleExecutionResult> results = optionalResults.get();
    assertFalse(results.isEmpty());
    assertEquals(3, results.size());
  }

  @Test
  @DisplayName("Test simple decision tree evaluation")
  void testEvaluateDecisionTree() {

    when(coreRuleEngine.evaluateCondition(anyString(), anyMap(), any())).thenReturn(true);
    when(coreRuleEngine.executeAction(anyString(), anyMap())).thenReturn(Optional.of("success"));

    // Define input variables
    Map<String, Object> inputVariables = new HashMap<>();
    inputVariables.put("value1", "ABC");
    inputVariables.put("value2", 101);
    inputVariables.put("value3", "2");

    // Evaluate all rules
    List<TreeExecutionResult> results = ruleEngine.evaluateMultipleDecisionTrees(inputVariables);

    // Assertions
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals("success", results.get(0).getResults().get("result"));

  }
}

