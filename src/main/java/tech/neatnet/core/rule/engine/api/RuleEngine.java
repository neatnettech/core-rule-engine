package tech.neatnet.core.rule.engine.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.neatnet.core.rule.engine.cache.RuleMatrixCache;
import tech.neatnet.core.rule.engine.core.CoreRuleEngine;
import tech.neatnet.core.rule.engine.domain.Metadata;
import tech.neatnet.core.rule.engine.domain.Rule;
import tech.neatnet.core.rule.engine.domain.RuleExecutionResult;
import tech.neatnet.core.rule.engine.domain.RuleMatrix;

@Slf4j
@Service
public class RuleEngine {

  private final CoreRuleEngine coreRuleEngine;
  private final RuleMatrixCache ruleMatrixCache;

  public RuleEngine(CoreRuleEngine coreRuleEngine, RuleMatrixCache ruleMatrixCache) {
    this.coreRuleEngine = coreRuleEngine;
    this.ruleMatrixCache = ruleMatrixCache;
  }

  public List<RuleExecutionResult> evaluateAllRules(Map<String, Object> inputVariables) {
    List<RuleExecutionResult> results = new ArrayList<>();
    Collection<RuleMatrix> ruleMatrices = ruleMatrixCache.getAllRuleMatrices();

    for (RuleMatrix matrix : ruleMatrices) {
      Metadata metadata = Metadata.builder()
          .inputVariables(new HashMap<>(inputVariables))
          .startTime(Instant.now())
          .build();

      boolean allConditionsMet = true;
      for (Rule rule : matrix.getRules()) {
        if (!coreRuleEngine.evaluateCondition(rule.getCondition(), inputVariables)) {
          allConditionsMet = false;
          break;
        }
      }

      Map<String, Object> combinedResults = new HashMap<>();
      if (allConditionsMet) {
        for (Rule rule : matrix.getRules()) {
          coreRuleEngine.executeAction(rule.getAction(), inputVariables)
              .ifPresent(result -> combinedResults.putAll((Map<String, Object>) result));
        }
      }

      metadata.setEndTime(Instant.now());

      RuleExecutionResult result = RuleExecutionResult.builder()
          .metadata(metadata)
          .ruleMatrix(matrix)
          .results(combinedResults)
          .build();

      results.add(result);
    }

    return results;
  }
}

