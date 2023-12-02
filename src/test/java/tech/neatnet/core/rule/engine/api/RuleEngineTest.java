package tech.neatnet.core.rule.engine.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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
import tech.neatnet.core.rule.engine.cache.RuleMatrixCache;
import tech.neatnet.core.rule.engine.core.CoreRuleEngine;
import tech.neatnet.core.rule.engine.domain.Rule;
import tech.neatnet.core.rule.engine.domain.RuleExecutionResult;
import tech.neatnet.core.rule.engine.domain.RuleMatrix;

@ExtendWith(MockitoExtension.class)
class RuleEngineTest {

  @Mock
  private CoreRuleEngine coreRuleEngine;

  @Mock
  private RuleMatrixCache ruleMatrixCache;

  private RuleEngine ruleEngine;

  @BeforeEach
  void setUp() {
    ruleEngine = new RuleEngine(coreRuleEngine, ruleMatrixCache);
  }

  @Test
  void testEvaluateAllRulesWithNoRules() {
    when(ruleMatrixCache.getAllRuleMatrices()).thenReturn(Collections.emptyList());

    List<RuleExecutionResult> results = ruleEngine.evaluateAllRules(new HashMap<>());

    assertTrue(results.isEmpty());
  }

  @Test
  void testEvaluateAllRulesWithTrueCondition() {
    Rule rule = Rule.builder()
        .condition("condition")
        .action("action")
        .build();

    RuleMatrix matrix = RuleMatrix.builder()
        .id(UUID.randomUUID())
        .rules(Collections.singletonList(rule))
        .build();

    when(ruleMatrixCache.getAllRuleMatrices()).thenReturn(Collections.singletonList(matrix));
    when(coreRuleEngine.evaluateCondition(anyString(), anyMap())).thenReturn(true);
    when(coreRuleEngine.executeAction(anyString(), anyMap())).thenReturn(
        Optional.of(Map.of("key", "value")));

    List<RuleExecutionResult> results = ruleEngine.evaluateAllRules(new HashMap<>());

    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertFalse(results.get(0).getResults().isEmpty());
  }

  @Test
  void testEvaluateAllRulesWithFalseCondition() {
    Rule rule = Rule.builder()
        .condition("condition")
        .action("action")
        .build();

    RuleMatrix matrix = RuleMatrix.builder()
        .id(UUID.randomUUID())
        .rules(Collections.singletonList(rule))
        .build();

    when(ruleMatrixCache.getAllRuleMatrices()).thenReturn(Collections.singletonList(matrix));
    when(coreRuleEngine.evaluateCondition(anyString(), anyMap())).thenReturn(false);

    List<RuleExecutionResult> results = ruleEngine.evaluateAllRules(new HashMap<>());

    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertTrue(results.get(0).getResults().isEmpty());
  }

  @Test
  @DisplayName("Test evaluateAllRules with complex inputVariables")
  void testEvaluateAllRulesWithComplexInputVariables() {
    // rule represents a single row in the rule matrix with an and condition
    Rule rule = Rule.builder()
        .condition("value1 == 'ABC'")
        .build();

    Rule rule1 = Rule.builder()
        .condition("value2 > 100")
        .build();

    Rule rule2 = Rule.builder()
        .condition("value3 in inValues")
        .inValues(List.of("1", "2", "3"))
        .build();

    RuleMatrix matrix = RuleMatrix.builder()
        .id(UUID.randomUUID())
        .rules(List.of(rule, rule1, rule2))
        .build();

    when(ruleMatrixCache.getAllRuleMatrices()).thenReturn(Collections.singletonList(matrix));
    when(coreRuleEngine.evaluateCondition(anyString(), anyMap())).thenReturn(true);
    when(coreRuleEngine.executeAction(anyString(), anyMap())).thenReturn(
        Optional.empty());

    Map<String, Object> inputVariables = new HashMap<>();
    inputVariables.put("value1", "ABC");
    inputVariables.put("value2", 100);
    inputVariables.put("value3", "1");

    List<RuleExecutionResult> results = ruleEngine.evaluateAllRules(inputVariables);

    assertFalse(results.isEmpty());
  }


}
