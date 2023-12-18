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
    private final Collection<Rule> ruleMatrices;

    public RuleEngine(CoreRuleEngine coreRuleEngine, RuleCache ruleCache,
                      RuleService ruleService) {
        this.coreRuleEngine = coreRuleEngine;
        this.ruleCache = ruleCache;
        this.ruleService = ruleService;
        this.ruleMatrices = ruleCache.getAllRules();
    }

    public List<Optional<RuleExecutionResult>> evaluateDecisionTrees(List<DecisionTree> decisionTrees, Map<String, Object> inputVariables) {
        List<Optional<RuleExecutionResult>> results = new ArrayList<>();

        for (DecisionTree decisionTree : decisionTrees) {
            // Start from the root of the decision tree
            RuleTree currentNode = decisionTree.getRules().get(0);

            // Traverse the decision tree
            while (!currentNode.getChildren().isEmpty()) {
                // Evaluate the condition using MVEL from CoreRuleEngine
                boolean conditionResult = coreRuleEngine.evaluateCondition(currentNode.getCondition(), inputVariables, Optional.empty());

                // Move to the next node based on the condition result
                currentNode = conditionResult ? currentNode.getChildren().get(0) : currentNode.getChildren().get(1);
            }

            // The current node is a leaf node, so it contains the result of the decision tree execution
            Map<String, Object> decisionTreeResults = currentNode.getResults();

            // Build the rule execution result
            RuleExecutionResult result = RuleExecutionResult.builder()
                    .metadata(Metadata.builder()
                            .inputVariables(new HashMap<>(inputVariables)) // Defensive copy
                            .startTime(Instant.now())
                            .endTime(Instant.now())
                            .build())
                    .rule(null)
                    .ruleTree(currentNode)
                    .results(decisionTreeResults)
                    .build();

            results.add(Optional.of(result));
        }

        return results;
    }

    public Optional<List<RuleExecutionResult>> evaluateDecisionMatrices(Map<String, Object> inputVariables) {
        List<RuleExecutionResult> results = new ArrayList<>();

        for (Rule matrix : ruleMatrices) {
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
}