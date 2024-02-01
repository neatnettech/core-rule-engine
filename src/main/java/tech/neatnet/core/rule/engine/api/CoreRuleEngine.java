package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.mvel2.MVEL;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
class CoreRuleEngine {

  public boolean evaluateCondition(String condition, Map<String, Object> data,
      Optional<List<Object>> inValues) {
    log.debug("Evaluating condition with data: {}", data);
    inValues.ifPresent(values -> data.put("inValues", values));
    boolean result = (boolean) MVEL.executeExpression(MVEL.compileExpression(condition), data);
    log.debug("Condition evaluation result: {}", result);
    return result;
  }

  public Optional<Object> executeAction(String action, Map<String, Object> data) {
    log.debug("Executing action with data: {}", data);
    Optional<Object> result = Optional.of(MVEL.executeExpression(MVEL.compileExpression(action), data));
    log.debug("Action execution result: {}", result);
    return result;
  }

  public boolean evaluate(String condition, Map<String, Object> data, Optional<String[]> inValues) {
    log.debug("Evaluating with data: {}", data);
    inValues.ifPresent(values -> data.put("inValues", values));
    boolean result = (boolean) MVEL.executeExpression(MVEL.compileExpression(condition), data);
    log.debug("Evaluation result: {}", result);
    return result;
  }
}