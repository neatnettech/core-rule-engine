package tech.neatnet.core.rule.engine.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CoreRuleEngineTest {

  private CoreRuleEngine coreRuleEngine;

  @BeforeEach
  void setUp() {
    coreRuleEngine = new CoreRuleEngine();
  }

  @Test
  void evaluateConditionTrue() {
    String condition = "value == true";
    Map<String, Object> data = new HashMap<>();
    data.put("value", true);
    assertTrue(coreRuleEngine.evaluateCondition(condition, data));
  }

  @Test
  void evaluateConditionFalse() {
    String condition = "value == false";
    Map<String, Object> data = new HashMap<>();
    data.put("value", true);
    assertFalse(coreRuleEngine.evaluateCondition(condition, data));
  }

  @Test
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
}
