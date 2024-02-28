package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.neatnet.core.rule.engine.domain.*;
import tech.neatnet.core.rule.engine.exceptions.RuleEngineClientProcessingException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RuleEngineClient {

    private final RuleEngine ruleEngine;
    private final RuleRepository ruleRepository;
    private final HitPolicy defaultHitPolicy;

    public RuleEngineClient(RuleEngine ruleEngine, RuleRepository ruleRepository) {
        this.ruleEngine = ruleEngine;
        this.ruleRepository = ruleRepository;
        this.defaultHitPolicy = HitPolicy.FIRST;
    }

    public List<RuleExecutionResult> evaluateRules(Map<String, Object> inputVariables, BaseRuleCategory ruleCategory, BaseRuleSubCategory subCategory, HitPolicy hitPolicy) throws RuleEngineClientProcessingException {
        log.debug("Evaluating rules with input variables: {}, ruleCategory: {}, subCategory: {}, hitPolicy: {}", inputVariables, ruleCategory, subCategory, hitPolicy);
        validateInputs(inputVariables, ruleCategory, subCategory, hitPolicy);
        return ruleEngine.evaluate(inputVariables, ruleCategory, subCategory, hitPolicy);
    }

    public List<RuleExecutionResult> evaluateRules(Map<String, Object> inputVariables, BaseRuleCategory ruleCategory, BaseRuleSubCategory subCategory) throws RuleEngineClientProcessingException {
        log.debug("Evaluating rules with input variables: {}, ruleCategory: {}, subCategory: {}, defaultHitPolicy: {}", inputVariables, ruleCategory, subCategory, defaultHitPolicy);
        validateInputs(inputVariables, ruleCategory, subCategory, defaultHitPolicy);
        return ruleEngine.evaluate(inputVariables, ruleCategory, subCategory, defaultHitPolicy);
    }
    public List<TreeExecutionResult> evaluateTree(Map<String, Object> inputVariables, BaseRuleCategory ruleCategory, BaseRuleSubCategory subCategory, HitPolicy hitPolicy) throws RuleEngineClientProcessingException {
        log.debug("Evaluating tree with input variables: {}, ruleCategory: {}, subCategory: {}, hitPolicy: {}", inputVariables, ruleCategory, subCategory, hitPolicy);
        validateInputs(inputVariables, ruleCategory, subCategory, hitPolicy);
        return ruleEngine.evaluateDecisionTree(inputVariables, ruleCategory, subCategory, hitPolicy);
    }
    public List<TreeExecutionResult> evaluateTree(Map<String, Object> inputVariables, BaseRuleCategory ruleCategory, BaseRuleSubCategory subCategory) throws RuleEngineClientProcessingException {
        log.debug("Evaluating tree with input variables: {}, ruleCategory: {}, subCategory: {}, hitPolicy: {}", inputVariables, ruleCategory, subCategory, defaultHitPolicy);
        validateInputs(inputVariables, ruleCategory, subCategory, defaultHitPolicy);
        return ruleEngine.evaluateDecisionTree(inputVariables, ruleCategory, subCategory, defaultHitPolicy);
    }
    private void validateInputs(Map<String, Object> inputVariables, BaseRuleCategory ruleCategory, BaseRuleSubCategory subCategory, HitPolicy hitPolicy) throws RuleEngineClientProcessingException {
        if (inputVariables == null || inputVariables.isEmpty()) {
            throw new RuleEngineClientProcessingException("Input variables cannot be empty");
        }
        if (ruleCategory == null) {
            throw new RuleEngineClientProcessingException("Rule category cannot be null");
        }
        if (subCategory == null) {
            throw new RuleEngineClientProcessingException("Rule sub category cannot be null");
        }
        if (hitPolicy == null) {
            throw new RuleEngineClientProcessingException("Hit policy cannot be null");
        }
    }

    public Rule saveRule(Rule rule) {
        return ruleRepository.save(rule);
    }

    public boolean saveRules(List<Rule> rules) {
        return ruleRepository.saveAll(rules).size() == rules.size();
    }

}