package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.neatnet.core.rule.engine.domain.Rule;

@Slf4j
@Service
public class RuleEngineClient {

    private final RuleRepository ruleRepository;

    public RuleEngineClient(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    public Rule saveRule(Rule rule) {
        log.debug("Saving rule: {}", rule);
        return ruleRepository.save(rule);
    }
}