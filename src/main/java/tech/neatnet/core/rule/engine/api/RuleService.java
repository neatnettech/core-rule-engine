package tech.neatnet.core.rule.engine.api;

import org.springframework.stereotype.Service;
import tech.neatnet.core.rule.engine.domain.Rule;

@Service
class RuleService {

    private final RuleRepository ruleRepository;

    public RuleService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    public Rule saveRule(Rule rule) {
        return ruleRepository.save(rule);
    }

    public String smokeTest() {
      return "smokeTest";
    }
}
