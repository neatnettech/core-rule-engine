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

    public RuleEngine(CoreRuleEngine coreRuleEngine, RuleCache ruleCache,
                      RuleService ruleService) {
        this.coreRuleEngine = coreRuleEngine;
        this.ruleCache = ruleCache;
        this.ruleService = ruleService;
        this.rules = ruleCache.getAllRules();
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
}