package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.neatnet.core.rule.engine.domain.*;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class RuleEngine {

    private final CoreRuleEngine coreRuleEngine;
    private final RuleCache ruleCache;
    private final RuleService ruleService;
    private final Collection<Rule> rules;
    private final Collection<Rule> decisionTrees;

    public RuleEngine(CoreRuleEngine coreRuleEngine, RuleCache ruleCache,
                      RuleService ruleService) {
        this.coreRuleEngine = coreRuleEngine;
        this.ruleCache = ruleCache;
        this.ruleService = ruleService;
        this.rules = ruleCache.getAllRules();
        this.decisionTrees = ruleCache.getAllDecisionTrees();
    }

    public Optional<List<RuleExecutionResult>> evaluateRules(Map<String, Object> inputVariables) {
        List<RuleExecutionResult> results = new ArrayList<>();

        for (Rule matrix : rules) {
            boolean allConditionsMet = true;
            Metadata metadata = Metadata.builder()
                    .inputVariables(new HashMap<>(inputVariables)) // Defensive copy
                    .startTime(Instant.now())
                    .build();

            for (Condition condition : matrix.getConditions()) {
                if (!coreRuleEngine.evaluateCondition(condition.getCondition(), inputVariables,
                        Optional.ofNullable(condition.getInValues()))) {
                    allConditionsMet = false;
                    break; // If any condition fails, stop checking further and move to next matrix
                }
            }

            Map<String, Object> matrixResults = matrix.getResults();
            if (!allConditionsMet) {
                matrixResults = Collections.emptyMap(); // No action results if conditions are not met
            }

            metadata.setEndTime(Instant.now());
            RuleExecutionResult result = RuleExecutionResult.builder()
                    .metadata(metadata)
                    .rule(matrix)
                    .results(matrixResults)
                    .build();

            results.add(result);
        }

        return Optional.of(results);
    }

    public List<TreeExecutionResult> evaluateMultipleDecisionTrees(Map<String, Object> inputVariables) {
        List<TreeExecutionResult> results = new ArrayList<>();
        for (Rule root : decisionTrees) {
            log.info("Evaluating decision tree: {}", root);
            for (Condition condition : root.getConditions()) {
                if (coreRuleEngine.evaluateCondition(condition.getCondition(), inputVariables,
                        Optional.ofNullable(condition.getInValues()))) {
                    results.add(evaluateDecisionTree(inputVariables, condition, root));
                }
            }
        }
        return results;
    }

    public TreeExecutionResult evaluateDecisionTree(Map<String, Object> inputVariables, Condition root, Rule rule) {
        TreeExecutionResult treeExecutionResult = new TreeExecutionResult();
        treeExecutionResult.setCondition(root);

        if (root.isLeaf()) {
            Map<String, Object> results = new HashMap<>();
            log.info("Evaluating action: {}", root.getAction());
            Optional<Object> actionResult = coreRuleEngine.executeAction(root.getAction(), inputVariables);
            actionResult.ifPresent(o -> results.put("result", o));
            treeExecutionResult.setResults(results);
            treeExecutionResult.setRule(rule);
            return treeExecutionResult;
        }

        boolean conditionResult = coreRuleEngine.evaluateCondition(root.getCondition(), inputVariables,
                Optional.ofNullable(root.getInValues()));

        if (conditionResult) {
            return evaluateDecisionTree(inputVariables, root.getTrueBranch(), rule);
        } else {
            return evaluateDecisionTree(inputVariables, root.getFalseBranch(), rule);
        }
    }
}