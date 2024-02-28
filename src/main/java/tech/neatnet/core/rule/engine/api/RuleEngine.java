package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.neatnet.core.rule.engine.domain.*;
import tech.neatnet.core.rule.engine.exceptions.RuleEngineClientProcessingException;

import java.util.*;

import static tech.neatnet.core.rule.engine.api.CoreRuleEngineHelper.mergeInputVariables;
import static tech.neatnet.core.rule.engine.exceptions.RuleEngineClientProcessingException.ERR_MSG_EMPTY_INPUT_VARIABLES;

@Slf4j
@Service
class RuleEngine {
    private final CoreRuleEngine coreRuleEngine;
    private final Collection<Rule> rules;
    public RuleEngine(CoreRuleEngine coreRuleEngine, RuleCache ruleCache) {
        this.coreRuleEngine = coreRuleEngine;
        this.rules = ruleCache.getAllRules();
        log.debug("RuleEngine initialized with {} rule(s)", rules.size());
    }

    public List<RuleExecutionResult> evaluate(Map<String, Object> inputVariables, BaseRuleCategory ruleCategory, BaseRuleSubCategory subCategory, HitPolicy hitPolicy) {
        long startTime = System.nanoTime();

        log.debug("Processing with input variables: {}, ruleCategory: {}, subCategory: {}, hitPolicy: {}", inputVariables, ruleCategory, subCategory, hitPolicy);
        List<RuleExecutionResult> results = new ArrayList<>();

        Iterator<Rule> ruleIterator = rules.stream()
                .filter(rule -> {
                    log.debug("Evaluating category {} {}", ruleCategory, rule.getRuleCategory());
                    log.debug("category filtering condition met {}", rule.getRuleCategory() == ruleCategory);
                    return rule.getRuleCategory() == ruleCategory;})
                .filter(rule -> {
                    log.debug("Evaluating sub category {} {}", subCategory, rule.getRuleSubCategory());
                    log.debug("sub category filtering condition met {}", rule.getRuleSubCategory() == subCategory);
                    return rule.getRuleSubCategory() == subCategory;})
                .filter(rule -> {
                    log.debug("Evaluating decision table {} {}", rule.getRuleType(), RuleType.DECISION_TABLE);
                    log.debug("decision table filtering condition met {}", rule.getRuleType() == RuleType.DECISION_TABLE);
                    return rule.getRuleType() == RuleType.DECISION_TABLE;})
                .iterator();

        while (ruleIterator.hasNext()) {
            Rule rule = ruleIterator.next();
            RuleExecutionResult ruleExecutionResult = evaluateRule(inputVariables, rule);
            results.add(ruleExecutionResult);
            if (hitPolicy == HitPolicy.FIRST && ruleExecutionResult.isRuleCriteriaMet()) {
                log.debug("Hit policy is FIRST. Stopping evaluation of rules");
                break;
            }
        }

        log.debug("Finished evaluating rules. Results: {}", results);

        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        log.debug("Evaluation of rules took {} nanoseconds", duration);

        return results;
    }

    private RuleExecutionResult evaluateRule(Map<String, Object> inputVariables, Rule rule) {
        long singleRuleStartTime = System.nanoTime();
        boolean allConditionsMet = rule.getConditions().stream()
                .allMatch(condition -> coreRuleEngine.evaluateCondition(condition.getCondition(), mergeInputVariables(inputVariables, condition.getInValues())));
        log.debug("All conditions met: {}", allConditionsMet);
        long singleRuleEndTime = System.nanoTime();

        Map<String, Object> ruleResults =
                allConditionsMet ? rule.getResults() : Collections.emptyMap();

        log.debug("Finished evaluating rule. Results: {}", ruleResults);
        return RuleExecutionResult.builder()
                .metadata(Metadata.builder()
                        .inputVariables(new HashMap<>(inputVariables))
                        .startTimeNanos(singleRuleStartTime)
                        .endTimeNanos(singleRuleEndTime)
                        .build())
                .rule(rule)
                .ruleCriteriaMet(allConditionsMet)
                .results(ruleResults)
                .build();
    }

    public List<TreeExecutionResult> evaluateDecisionTree(Map<String, Object> inputVariables, BaseRuleCategory ruleCategory, BaseRuleSubCategory subCategory, HitPolicy hitPolicy) {
        long startTime = System.nanoTime();

        log.debug("Evaluating multiple decision trees with input variables: {}", inputVariables);
        List<TreeExecutionResult> results = new ArrayList<>();

        Iterator<Rule> ruleIterator = rules.stream()
                .filter(rule -> rule.getRuleCategory().equals(ruleCategory))
                .filter(rule -> rule.getRuleType() == RuleType.DECISION_TREE)
                .filter(rule -> rule.getRuleSubCategory() == subCategory)
                .iterator();

        while (ruleIterator.hasNext()) {
            Rule rule = ruleIterator.next();

            for (Condition condition : rule.getConditions()) {
                TreeExecutionResult tet = evaluateTree(inputVariables, condition, rule, new ArrayList<>());
                results.add(tet);
                if (hitPolicy == HitPolicy.FIRST && tet.isRuleCriteriaMet()) {
                    log.debug("Hit policy is FIRST. Stopping evaluation of decision trees");
                    break;
                }
            }
        }

        log.debug("Finished evaluating multiple decision trees. Results: {}", results);

        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        log.debug("Evaluation of multiple decision trees took {} nanoseconds", duration);

        return results;
    }

    private TreeExecutionResult evaluateTree(Map<String, Object> inputVariables,
                                             Condition condition, Rule rule, List<Condition> executedNodes) {
        executedNodes.add(condition);
        if (condition.isLeaf()) {
            Map<String, Object> results = new HashMap<>();
            coreRuleEngine.executeAction(condition.getAction(), inputVariables)
                    .ifPresent(o -> results.put("result", o));
            log.debug("Finished evaluating decision tree. Results: {}", results);
            return TreeExecutionResult
                    .builder()
                    .rule(rule)
                    .condition(condition)
                    .results(results)
                    .executedNodes(new ArrayList<>(executedNodes))
                    .ruleCriteriaMet(true)
                    .build();
        }
        log.debug("Evaluating condition: {}", condition.getCondition());
        Condition nextCondition;
        if (coreRuleEngine.evaluateCondition(condition.getCondition(), mergeInputVariables(inputVariables, condition.getInValues()))) {
            nextCondition = condition.getTrueBranch();
        } else {
            nextCondition = condition.getFalseBranch();
        }

        return evaluateTree(inputVariables, nextCondition, rule, executedNodes);
    }
}