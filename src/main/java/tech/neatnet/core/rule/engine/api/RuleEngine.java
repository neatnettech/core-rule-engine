package tech.neatnet.core.rule.engine.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.neatnet.core.rule.engine.cache.RuleMatrixCache;
import tech.neatnet.core.rule.engine.core.CoreRuleEngine;
import tech.neatnet.core.rule.engine.domain.MatrixCategory;
import tech.neatnet.core.rule.engine.domain.Metadata;
import tech.neatnet.core.rule.engine.domain.Rule;
import tech.neatnet.core.rule.engine.domain.RuleExecutionResult;
import tech.neatnet.core.rule.engine.domain.RuleMatrix;
import tech.neatnet.core.rule.engine.service.RuleMatrixService;

@Slf4j
@Service
public class RuleEngine {

  private final CoreRuleEngine coreRuleEngine;
  private final RuleMatrixCache ruleMatrixCache;
  private final RuleMatrixService ruleMatrixService;

  public RuleEngine(CoreRuleEngine coreRuleEngine, RuleMatrixCache ruleMatrixCache,
      RuleMatrixService ruleMatrixService) {
    this.coreRuleEngine = coreRuleEngine;
    this.ruleMatrixCache = ruleMatrixCache;
    this.ruleMatrixService = ruleMatrixService;
  }

  public Optional<List<RuleExecutionResult>> evaluateAllRules(Map<String, Object> inputVariables) {
    List<RuleExecutionResult> results = new ArrayList<>();
    Collection<RuleMatrix> ruleMatrices = ruleMatrixCache.getAllRuleMatrices();

    for (RuleMatrix matrix : ruleMatrices) {
      boolean allConditionsMet = true;
      Metadata metadata = Metadata.builder()
          .inputVariables(new HashMap<>(inputVariables)) // Defensive copy
          .startTime(Instant.now())
          .build();

      for (Rule rule : matrix.getRules()) {
        if (!coreRuleEngine.evaluateCondition(rule.getCondition(), inputVariables,
            Optional.ofNullable(rule.getInValues()))) {
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
          .ruleMatrix(matrix)
          .results(matrixResults)
          .build();

      results.add(result);
    }

    return Optional.of(results);
  }

  public RuleMatrix saveRuleMatrix() {

    Rule rule1 = Rule.builder()
        .condition("value > 10")
        .inValues(Arrays.asList("10", "20"))
        .build();

    RuleMatrix sampleMatrix = RuleMatrix.builder()
        .id(UUID.randomUUID())
        .dateCreated(Instant.now())
        .dateModified(Instant.now())
        .modifiedBy("user123")
        .version(1)
        .name("Sample Matrix")
        .description("This is a sample rule matrix")
        .category(MatrixCategory.DUMMY_SIGNAL)
        .rules(Collections.singletonList(rule1))
        .results(new HashMap<String, Object>() {{
          put("signal", "value");
        }})
        .build();

    ruleMatrixService.saveRuleMatrix(sampleMatrix);
    return ruleMatrixService.saveRuleMatrix(sampleMatrix);
  }
}

