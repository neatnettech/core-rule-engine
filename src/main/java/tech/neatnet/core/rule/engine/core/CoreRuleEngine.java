package tech.neatnet.core.rule.engine.core;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.mvel2.MVEL;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CoreRuleEngine {

  /**
   * Evaluates the condition for a given rule.
   *
   * @param condition The condition as an MVEL expression.
   * @param data      The input variables for the rule.
   * @return The result of the condition evaluation.
   */
  public boolean evaluateCondition(String condition, Map<String, Object> data) {
    Serializable compiledCondition;
    try {
      compiledCondition = MVEL.compileExpression(condition);
      return (boolean) MVEL.executeExpression(compiledCondition, data);
    } catch (Exception e) {
      log.error("Failed to evaluate condition: {}", condition, e);
      throw new RuntimeException("Failed to evaluate condition", e);
    }
  }

  /**
   * Evaluates the condition for a given rule.
   *
   * @param condition The condition as an MVEL expression.
   * @param data      The input variables for the rule.
   * @param inValues  The list of values for list-based conditions.
   * @return The result of the condition evaluation.
   */
  public boolean evaluateCondition(String condition, Map<String, Object> data,
      List<String> inValues) {
    Serializable compiledCondition;
    try {
      data.put("inValues", inValues);
      compiledCondition = MVEL.compileExpression(condition);
      return (boolean) MVEL.executeExpression(compiledCondition, data);
    } catch (Exception e) {
      log.error("Failed to evaluate condition: {}", condition, e);
      throw new RuntimeException("Failed to evaluate condition", e);
    }
  }

  /**
   * Executes the action for a given rule.
   *
   * @param action The action as an MVEL expression.
   * @param data   The input variables for the rule.
   * @return The result of the action execution.
   */
  public Optional<Object> executeAction(String action, Map<String, Object> data) {
    Serializable compiledAction;
    try {
      compiledAction = MVEL.compileExpression(action);
      return Optional.of(MVEL.executeExpression(compiledAction, data));
    } catch (Exception e) {
      log.error("Failed to execute action: {}", action, e);
      throw new RuntimeException("Failed to execute action", e);
    }
  }
}
