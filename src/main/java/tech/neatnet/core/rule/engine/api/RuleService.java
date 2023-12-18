package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.neatnet.core.rule.engine.domain.Rule;

@Slf4j
@Service
class RuleService {

  private final RuleRepository ruleRepository;

  public RuleService(RuleRepository ruleRepository) {
    this.ruleRepository = ruleRepository;
  }

  public Rule saveRule(Rule rule) {
    log.info("Saving RuleMatrix: {}", rule);
    return ruleRepository.save(rule);
  }

}
