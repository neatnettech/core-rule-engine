package tech.neatnet.core.rule.engine.api;

import org.springframework.stereotype.Service;
import tech.neatnet.core.rule.engine.domain.Rule;

@Service
public class RuleEngineClient {

    private final RuleService ruleService;

    public RuleEngineClient(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    public Rule saveRule(Rule rule) {
      return ruleService.saveRule(rule);
    }

    public String smokeTest() {
      return ruleService.smokeTest();
    }
}