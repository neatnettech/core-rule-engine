package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.neatnet.core.rule.engine.domain.*;

import java.util.*;

@Slf4j
@Service
public class RuleEngine {

  private final CoreRuleEngine coreRuleEngine;
  private final Collection<Rule> rules;

  public RuleEngine(CoreRuleEngine coreRuleEngine, RuleCache ruleCache) {
    this.coreRuleEngine = coreRuleEngine;
    this.rules = ruleCache.getAllRules();
    log.debug("RuleEngine initialized with {} rule(s)", rules.size());
  }

  public Optional<List<RuleExecutionResult>> evaluateRules(Map<String, Object> inputVariables) {
    long startTime = System.nanoTime();

    log.debug("Evaluating rules with input variables: {}", inputVariables);
    List<RuleExecutionResult> results = new ArrayList<>();

    for (Rule rule : rules) {
      long singleRuleStartTime = System.nanoTime();
      boolean allConditionsMet = rule.getConditions().stream()
              .allMatch(condition -> coreRuleEngine.evaluateCondition(condition.getCondition(),
                      inputVariables,
                      Optional.ofNullable(condition.getInValues())));
      long singleRuleEndTime = System.nanoTime();

      Map<String, Object> ruleResults =
              allConditionsMet ? rule.getResults() : Collections.emptyMap();

      RuleExecutionResult result = RuleExecutionResult.builder()
              .metadata(Metadata.builder()
                      .inputVariables(new HashMap<>(inputVariables))
                      .startTimeNanos(singleRuleStartTime)
                      .endTimeNanos(singleRuleEndTime)
                      .build())
              .rule(rule)
              .results(ruleResults)
              .build();

      results.add(result);
    }

    log.debug("Finished evaluating rules. Results: {}", results);

    long endTime = System.nanoTime();
    long duration = endTime - startTime;
    log.debug("Evaluation of rules took {} nanoseconds", duration);

    return Optional.of(results);
  }

  public List<TreeExecutionResult> evaluateMultipleDecisionTrees(Map<String, Object> inputVariables) {
    long startTime = System.nanoTime();

    log.debug("Evaluating multiple decision trees with input variables: {}", inputVariables);
    List<TreeExecutionResult> results = new ArrayList<>();
    for (Rule root : rules) {
      root.getConditions().stream()
              .filter(condition -> coreRuleEngine.evaluateCondition(condition.getCondition(),
                      inputVariables,
                      Optional.ofNullable(condition.getInValues())))
              .map(condition -> evaluateDecisionTree(inputVariables, condition, root))
              .forEach(results::add);
    }
    log.debug("Finished evaluating multiple decision trees. Results: {}", results);

    long endTime = System.nanoTime();
    long duration = endTime - startTime;
    log.debug("Evaluation of multiple decision trees took {} nanoseconds", duration);

    return results;
  }

  private TreeExecutionResult evaluateDecisionTree(Map<String, Object> inputVariables,
      Condition root, Rule rule) {
    log.debug("Evaluating decision tree with input variables: {}", inputVariables);
    if (root.isLeaf()) {
      Map<String, Object> results = new HashMap<>();
      coreRuleEngine.executeAction(root.getAction(), inputVariables)
          .ifPresent(o -> results.put("result", o));

      log.debug("Finished evaluating decision tree. Results: {}", results);
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