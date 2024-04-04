package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.neatnet.core.rule.engine.domain.*;
import tech.neatnet.core.rule.engine.exceptions.RuleEngineClientProcessingException;

import java.util.List;
import java.util.Map;

public interface RuleEngineClient {

    List<RuleExecutionResult> evaluateRules(Map<String, Object> inputVariables, BaseRuleCategory ruleCategory, BaseRuleSubCategory subCategory, HitPolicy hitPolicy) throws RuleEngineClientProcessingException;

    Rule saveRule(Rule rule) throws RuleEngineClientProcessingException;

}
