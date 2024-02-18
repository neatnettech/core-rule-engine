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
    private final Collection<Rule> matrices;

    private final Collection<Rule> trees;

    public RuleEngine(CoreRuleEngine coreRuleEngine, RuleCache ruleCache) {
        this.coreRuleEngine = coreRuleEngine;
        this.matrices = ruleCache.getAllMatrices();
        this.trees = ruleCache.getAllTrees();
        log.debug("RuleEngine initialized with \n{} matrice(s)\n{} tree(s)", matrices.size(), trees.size());
    }

    public List<RuleExecutionResult> evaluateRules(Map<String, Object> inputVariables) {
        long startTime = System.nanoTime();

        log.debug("Evaluating rules with input variables: {}", inputVariables);
        List<RuleExecutionResult> results = new ArrayList<>();

        for (Rule rule : matrices) {
            long singleRuleStartTime = System.nanoTime();
            boolean allConditionsMet = rule.getConditions().stream()
                    .allMatch(condition -> coreRuleEngine.evaluateCondition(condition.getCondition(), mergeInputVariables(inputVariables, condition.getInValues())));
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

        return results;
    }

    public List<TreeExecutionResult> evaluateMultipleDecisionTrees(Map<String, Object> inputVariables) {
        long startTime = System.nanoTime();

        log.debug("Evaluating multiple decision trees with input variables: {}", inputVariables);
        List<TreeExecutionResult> results = new ArrayList<>();
        for (Rule root : trees) {
            root.getConditions().stream()
                    .map(condition ->
                            {
                                log.debug("current condition: {}", condition);
                                return evaluateDecisionTree(inputVariables, condition, root);
                            }
                    )
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
        log.debug("Evaluating condition: {}", root.getCondition());
        Condition nextCondition;
        if (coreRuleEngine.evaluateCondition(root.getCondition(), mergeInputVariables(inputVariables, root.getInValues()))) {
            nextCondition = root.getTrueBranch();
        } else {
            nextCondition = root.getFalseBranch();
        }

        return evaluateDecisionTree(inputVariables, nextCondition, rule);
    }
}