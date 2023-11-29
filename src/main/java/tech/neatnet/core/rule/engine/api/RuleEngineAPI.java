package tech.neatnet.core.rule.engine.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.neatnet.core.rule.engine.cache.RuleMatrixCache;
import tech.neatnet.core.rule.engine.core.CoreRuleEngine;
import tech.neatnet.core.rule.engine.domain.Metadata;
import tech.neatnet.core.rule.engine.domain.Rule;
import tech.neatnet.core.rule.engine.domain.RuleExecutionResult;
import tech.neatnet.core.rule.engine.domain.RuleMatrix;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class RuleEngineAPI {

    private final CoreRuleEngine coreRuleEngine;
    private final RuleMatrixCache ruleMatrixCache;

    public RuleEngineAPI(CoreRuleEngine coreRuleEngine, RuleMatrixCache ruleMatrixCache) {
        this.coreRuleEngine = coreRuleEngine;
        this.ruleMatrixCache = ruleMatrixCache;
    }

    public List<RuleExecutionResult> evaluateAllRules(Map<String, Object> inputVariables) {
        List<RuleExecutionResult> results = new ArrayList<>();
        Collection<RuleMatrix> ruleMatrices = ruleMatrixCache.getAllRuleMatrices();

        for (RuleMatrix matrix : ruleMatrices) {
            for (Rule rule : matrix.getRules()) {
                Metadata metadata = Metadata.builder()
                        .inputVariables(new HashMap<>(inputVariables)) // Defensive copy
                        .startTime(Instant.now())
                        .build();

                Optional<Object> optionalActionResult = Optional.empty();
                if (coreRuleEngine.evaluateCondition(rule.getCondition(), inputVariables)) {
                    optionalActionResult = coreRuleEngine.executeAction(rule.getAction(), inputVariables);
                }

                metadata.setEndTime(Instant.now());

                RuleExecutionResult result = RuleExecutionResult.builder()
                        .metadata(metadata)
                        .rule(rule)
                        .results(optionalActionResult.map(o -> (Map<String, Object>) o).orElse(Collections.emptyMap()))
                        .build();

                results.add(result);
            }
        }

        return results;
    }

}

