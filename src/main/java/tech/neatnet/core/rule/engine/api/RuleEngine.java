package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.neatnet.core.rule.engine.domain.*;

import java.util.*;

import static tech.neatnet.core.rule.engine.api.CoreRuleEngineHelper.mergeInputVariables;

@Slf4j
@Service
class RuleEngine {

    private final CoreRuleEngine coreRuleEngine;
    private final Collection<Rule> rules;

    public RuleEngine(CoreRuleEngine coreRuleEngine, RuleCache ruleCache) {
        this.coreRuleEngine = coreRuleEngine;
        this.rules = ruleCache.getAllRules();
        log.debug("RuleEngine initialized with {} rule(s)", rules.size());
    }

    public List<RuleExecutionResult> evaluateRules(Map<String, Object> inputVariables, String ruleCategory) {
        long startTime = System.nanoTime();

        log.debug("Evaluating rules with input variables: {}", inputVariables);
        List<RuleExecutionResult> results = new ArrayList<>();

        rules.stream()
                .filter(rule -> rule.getRuleCategory().equals(ruleCategory))
                .filter(rule -> rule.getRuleType() == RuleType.DECISION_TABLE)
                .forEach(rule -> {
                    results.add(executeRuleWithSingleResult(inputVariables, rule));
                });

        log.debug("Finished evaluating rules. Results: {}", results);

        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        log.debug("Evaluation of rules took {} nanoseconds", duration);

        return results;
    }

    private RuleExecutionResult executeRuleWithSingleResult(Map<String, Object> inputVariables, Rule rule) {
        long singleRuleStartTime = System.nanoTime();
        boolean allConditionsMet = rule.getConditions().stream()
                .allMatch(condition -> coreRuleEngine.evaluateCondition(condition.getCondition(), mergeInputVariables(inputVariables, condition.getInValues())));
        long singleRuleEndTime = System.nanoTime();

        Map<String, Object> ruleResults =
                allConditionsMet ? rule.getResults() : Collections.emptyMap();

        return RuleExecutionResult.builder()
                .metadata(Metadata.builder()
                        .inputVariables(new HashMap<>(inputVariables))
                        .startTimeNanos(singleRuleStartTime)
                        .endTimeNanos(singleRuleEndTime)
                        .build())
                .rule(rule)
                .results(ruleResults)
                .build();
    }

    public List<TreeExecutionResult> evaluateDecisionTree(Map<String, Object> inputVariables, BaseRuleCategory ruleCategory) {
        long startTime = System.nanoTime();

        log.debug("Evaluating multiple decision trees with input variables: {}", inputVariables);
        List<TreeExecutionResult> results = new ArrayList<>();

        log.debug("Finished evaluating multiple decision trees. Results: {}", results);

        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        log.debug("Evaluation of multiple decision trees took {} nanoseconds", duration);

        return results;
    }

    private TreeExecutionResult evaluateTree(Map<String, Object> inputVariables,
                                             Condition condition, Rule rule, List<Condition> executedNodes) {
        executedNodes.add(condition);
        if (condition.isLeaf()) {
            Map<String, Object> results = new HashMap<>();
            coreRuleEngine.executeAction(condition.getAction(), inputVariables)
                    .ifPresent(o -> results.put("result", o));
            log.debug("Finished evaluating decision tree. Results: {}", results);
            return TreeExecutionResult
                    .builder()
                    .rule(rule)
                    .condition(condition)
                    .results(results)
                    .executedNodes(new ArrayList<>(executedNodes))
                    .build();
        }
        log.debug("Evaluating condition: {}", condition.getCondition());
        Condition nextCondition;
        if (coreRuleEngine.evaluateCondition(condition.getCondition(), mergeInputVariables(inputVariables, condition.getInValues()))) {
            nextCondition = condition.getTrueBranch();
        } else {
            nextCondition = condition.getFalseBranch();
        }

        return evaluateTree(inputVariables, nextCondition, rule, executedNodes);
    }
}