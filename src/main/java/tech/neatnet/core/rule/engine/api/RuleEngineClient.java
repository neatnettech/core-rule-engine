package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.neatnet.core.rule.engine.domain.Rule;
import tech.neatnet.core.rule.engine.domain.RuleExecutionResult;
import tech.neatnet.core.rule.engine.domain.TreeExecutionResult;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RuleEngineClient {

    private final RuleRepository ruleRepository;
    private final RuleEngine ruleEngine;

    public RuleEngineClient(RuleRepository ruleRepository, RuleEngine ruleEngine) {
        this.ruleRepository = ruleRepository;
        this.ruleEngine = ruleEngine;
    }

    public List<RuleExecutionResult> evaluateRules(Map<String, Object> inputVariables) {
        log.debug("Evaluating rules with input variables: {}", inputVariables);
        return ruleEngine.evaluateRules(inputVariables);
    }

    public List<TreeExecutionResult> evaluateMultipleDecisionTrees(Map<String, Object> inputVariables) {
        log.debug("Evaluating multiple decision trees with input variables: {}", inputVariables);
        return ruleEngine.evaluateMultipleDecisionTrees(inputVariables);
    }

    public Rule saveRule(Rule rule) {
        log.debug("Saving rule: {}", rule);
        return ruleRepository.save(rule);
    }
}