package tech.neatnet.core.rule.engine.api;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.neatnet.core.rule.engine.domain.MatrixCategory;
import tech.neatnet.core.rule.engine.domain.Rule;
import tech.neatnet.core.rule.engine.domain.RuleMatrix;

@Slf4j
@Service
public class RuleController {

  private final RuleMatrixService ruleMatrixService;

  public RuleController(RuleMatrixService ruleMatrixService) {
    this.ruleMatrixService = ruleMatrixService;
  }

  public RuleMatrix saveRuleMatrix() {

    Rule rule1 = Rule.builder()
        .condition("value > 10")
        .build();

    Rule rule2 = Rule.builder()
        .condition("value2 == 'test'")
        .build();

    Rule rule3 = Rule.builder()
        .condition("value3 in inValues")
        .inValues(Arrays.asList("test1", "test2"))
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
        .rules(List.of(rule1, rule2, rule3))
        .results(new HashMap<String, Object>() {{
          put("signal", "valid");
        }})
        .build();
    log.info("RuleMatrix: {}", sampleMatrix);
    return ruleMatrixService.saveRuleMatrix(sampleMatrix);
  }


  public void updateRuleMatrix(RuleMatrix ruleMatrix) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

}
