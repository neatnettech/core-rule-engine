package tech.neatnet.core.rule.engine.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import tech.neatnet.core.rule.engine.domain.Condition;
import tech.neatnet.core.rule.engine.domain.Metadata;
import tech.neatnet.core.rule.engine.domain.Rule;
import tech.neatnet.core.rule.engine.domain.RuleExecutionResult;
import tech.neatnet.core.rule.engine.domain.TreeExecutionResult;

@Service
public class RuleEngine {

  private final CoreRuleEngine coreRuleEngine;
  private final Collection<Rule> rules;

  public RuleEngine(CoreRuleEngine coreRuleEngine, RuleCache ruleCache) {
    this.coreRuleEngine = coreRuleEngine;
    this.rules = ruleCache.getAllRules();
  }

  public Optional<List<RuleExecutionResult>> evaluateRules(Map<String, Object> inputVariables) {
    List<RuleExecutionResult> results = new ArrayList<>();

    for (Rule rule : rules) {
      boolean allConditionsMet = rule.getConditions().stream()
          .allMatch(condition -> coreRuleEngine.evaluateCondition(condition.getCondition(),
              inputVariables,
              Optional.ofNullable(condition.getInValues())));

      Map<String, Object> ruleResults =
          allConditionsMet ? rule.getResults() : Collections.emptyMap();

      RuleExecutionResult result = RuleExecutionResult.builder()
          .metadata(Metadata.builder()
              .inputVariables(new HashMap<>(inputVariables))
              .startTime(Instant.now())
              .endTime(Instant.now())
              .build())
          .rule(rule)
          .results(ruleResults)
          .build();

      results.add(result);
    }

    return Optional.of(results);
  }

  public List<TreeExecutionResult> evaluateMultipleDecisionTrees(
      Map<String, Object> inputVariables) {
    List<TreeExecutionResult> results = new ArrayList<>();
    for (Rule root : rules) {
      root.getConditions().stream()
          .filter(condition -> coreRuleEngine.evaluateCondition(condition.getCondition(),
              inputVariables,
              Optional.ofNullable(condition.getInValues())))
          .map(condition -> evaluateDecisionTree(inputVariables, condition, root))
          .forEach(results::add);
    }
    return results;
  }

  private TreeExecutionResult evaluateDecisionTree(Map<String, Object> inputVariables,
      Condition root, Rule rule) {
    if (root.isLeaf()) {
      Map<String, Object> results = new HashMap<>();
      coreRuleEngine.executeAction(root.getAction(), inputVariables)
          .ifPresent(o -> results.put("result", o));

      return TreeExecutionResult
          .builder()
          .rule(rule)
          .condition(root)
          .results(results)
          .build();
    }

    Condition nextCondition = coreRuleEngine.evaluateCondition(root.getCondition(), inputVariables,
        Optional.ofNullable(root.getInValues())) ? root.getTrueBranch() : root.getFalseBranch();

    return evaluateDecisionTree(inputVariables, nextCondition, rule);
  }
}