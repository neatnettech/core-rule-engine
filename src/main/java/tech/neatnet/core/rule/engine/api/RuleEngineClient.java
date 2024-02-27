package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.neatnet.core.rule.engine.domain.BaseRuleCategory;
import tech.neatnet.core.rule.engine.domain.Rule;
import tech.neatnet.core.rule.engine.domain.RuleExecutionResult;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RuleEngineClient {

    private final RuleEngine ruleEngine;
    private final RuleRepository ruleRepository;

    public RuleEngineClient(RuleEngine ruleEngine, RuleRepository ruleRepository) {
        this.ruleEngine = ruleEngine;
        this.ruleRepository = ruleRepository;
    }

    public List<RuleExecutionResult> evaluateRules(Map<String, Object> inputVariables, BaseRuleCategory ruleCategory) {
        log.debug("Evaluating rules with input variables: {}", inputVariables);
        return ruleEngine.evaluateRules(inputVariables, ruleCategory);
    }

    public Rule saveRule(Rule rule) {
        return ruleRepository.save(rule);
    }

}