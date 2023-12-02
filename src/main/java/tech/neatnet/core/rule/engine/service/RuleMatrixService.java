package tech.neatnet.core.rule.engine.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.neatnet.core.rule.engine.domain.RuleMatrix;
import tech.neatnet.core.rule.engine.repositories.RuleMatrixRepository;

@Slf4j
@Service
public class RuleMatrixService {

  private final RuleMatrixRepository ruleMatrixRepository;

  public RuleMatrixService(RuleMatrixRepository ruleMatrixRepository) {
    this.ruleMatrixRepository = ruleMatrixRepository;
  }

  public RuleMatrix saveRuleMatrix(RuleMatrix ruleMatrix) {
    log.info("Saving RuleMatrix: {}", ruleMatrix);
    return ruleMatrixRepository.save(ruleMatrix);
  }

}
