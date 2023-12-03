package tech.neatnet.core.rule.engine.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mvel2.MVEL;
import tech.neatnet.core.rule.engine.domain.Rule;

@ExtendWith(MockitoExtension.class)
class CoreRuleEngineTest {

  private CoreRuleEngine coreRuleEngine;

  public static void evaluateRule(Rule rule, Map<String, Object> context) {
    Serializable compiledExpression = MVEL.compileExpression(rule.getCondition());
    boolean result = MVEL.executeExpression(compiledExpression, context, Boolean.class);

    if (result) {
      System.out.println("Condition met. Performing action: " + rule.getAction());
    } else {
      System.out.println("Condition not met.");
    }
  }

  @BeforeEach
  void setUp() {
    coreRuleEngine = new CoreRuleEngine();
  }

  @Test
  @DisplayName("Test evaluateCondition with inValues matching")
  void evaluateInValuesTrue() {

    Rule rule = Rule.builder()
        .condition("inValues contains value")
        .inValues(Arrays.asList("value1", "value2"))
        .build();

    Map<String, Object> context = new HashMap<>();
    context.put("value", "value1");

    assertTrue(coreRuleEngine.evaluateCondition(rule.getCondition(), context,
        Optional.of(rule.getInValues())));
  }

  @Test
  @DisplayName("Test evaluateCondition with inValues not matching")
  void evaluateInValuesFalse() {

    Rule rule = Rule.builder()
        .condition("inValues contains value")
        .inValues(Arrays.asList("value1", "value2"))
        .build();

    Map<String, Object> context = new HashMap<>();
    context.put("value", "value3");

    assertFalse(coreRuleEngine.evaluateCondition(rule.getCondition(), context,
        Optional.of(rule.getInValues())));
  }

  @Test
  @DisplayName("Test evaluateCondition with true condition")
  void evaluateConditionTrue() {

    Rule rule = Rule.builder()
        .condition("value == true")
        .build();

    Map<String, Object> context = new HashMap<>();
    context.put("value", true);

    assertTrue(coreRuleEngine.evaluateCondition(rule.getCondition(), context, Optional.empty()));
  }

  @Test
  @DisplayName("Test evaluateCondition with false condition")
  void evaluateConditionFalse() {

    Rule rule = Rule.builder()
        .condition("value == false")
        .build();

    Map<String, Object> context = new HashMap<>();
    context.put("value", true);
    assertFalse(coreRuleEngine.evaluateCondition(rule.getCondition(), context, Optional.empty()));
  }

  @Test
  @DisplayName("Test evaluateCondition with action")
  void executeActionSuccess() {
    String action = "result = 'success'";
    Map<String, Object> data = new HashMap<>();
    Optional<Object> result = coreRuleEngine.executeAction(action, data);
    assertTrue(result.isPresent());
    assertEquals("success", result.get());
  }

  @Test
  void executeActionException() {
    String action = "invalid expression";
    Map<String, Object> data = new HashMap<>();

    Exception exception = assertThrows(RuntimeException.class, () -> {
      coreRuleEngine.executeAction(action, data);
    });

    String expectedMessage = "Failed to execute action";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }


  @Test
  @DisplayName("Test evaluateCondition with multiple rules")
  void evaluateConditionMultipleRules() {

    Rule rule1 = Rule.builder()
        .condition("value1 > 10")
        .build();

    Rule rule2 = Rule.builder()
        .condition("value2 == 'test'")
        .build();

    Rule rule3 = Rule.builder()
        .condition("inValues contains value3")
        .inValues(Arrays.asList("test1", "test2"))
        .build();

    Map<String, Object> context = new HashMap<>();
    context.put("value1", 11);
    context.put("value2", "test");
    context.put("value3", "test1");

    assertTrue(coreRuleEngine.evaluateCondition(rule1.getCondition(), context, Optional.empty()));
    assertTrue(coreRuleEngine.evaluateCondition(rule2.getCondition(), context, Optional.empty()));
    assertTrue(coreRuleEngine.evaluateCondition(rule3.getCondition(), context,
        Optional.of(rule3.getInValues())));
  }
}
