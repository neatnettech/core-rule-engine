package tech.neatnet.core.rule.engine.api;

import tech.neatnet.core.rule.engine.domain.HitPolicy;
import tech.neatnet.core.rule.engine.domain.Rule;
import tech.neatnet.core.rule.engine.domain.RuleExecutionResult;
import tech.neatnet.core.rule.engine.exceptions.RuleEngineClientProcessingException;

import java.util.List;
import java.util.Map;

public interface RuleEngineClient {

    List<RuleExecutionResult> evaluateRules(Map<String, Object> inputVariables, BaseRuleCategory ruleCategory, BaseRuleSubCategory subCategory, HitPolicy hitPolicy) throws RuleEngineClientProcessingException;

    Rule saveRule(Rule rule) throws RuleEngineClientProcessingException;

}
