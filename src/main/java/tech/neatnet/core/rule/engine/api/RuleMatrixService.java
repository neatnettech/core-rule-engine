package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.neatnet.core.rule.engine.domain.RuleMatrix;
import tech.neatnet.core.rule.engine.api.RuleMatrixRepository;

@Slf4j
@Service
class RuleMatrixService {

  private final RuleMatrixRepository ruleMatrixRepository;

  public RuleMatrixService(RuleMatrixRepository ruleMatrixRepository) {
    this.ruleMatrixRepository = ruleMatrixRepository;
  }

  public RuleMatrix saveRuleMatrix(RuleMatrix ruleMatrix) {
    log.info("Saving RuleMatrix: {}", ruleMatrix);
    return ruleMatrixRepository.save(ruleMatrix);
  }

}
