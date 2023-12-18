package tech.neatnet.core.rule.engine.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.mvel2.MVEL;
import org.springframework.stereotype.Component;

@Component
class CoreRuleEngine {

  public boolean evaluateCondition(String condition, Map<String, Object> data,
      Optional<List<Object>> inValues) {
    inValues.ifPresent(values -> data.put("inValues", values));
    return (boolean) MVEL.executeExpression(MVEL.compileExpression(condition), data);
  }

  public Optional<Object> executeAction(String action, Map<String, Object> data) {
    return Optional.of(MVEL.executeExpression(MVEL.compileExpression(action), data));
  }

  public boolean evaluate(String condition, Map<String, Object> data, Optional<String[]> inValues) {
    inValues.ifPresent(values -> data.put("inValues", values));
    return (boolean) MVEL.executeExpression(MVEL.compileExpression(condition), data);
  }
}