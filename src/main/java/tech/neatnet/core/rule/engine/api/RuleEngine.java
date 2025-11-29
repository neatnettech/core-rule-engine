package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.neatnet.core.rule.engine.domain.*;

import java.util.*;

import static tech.neatnet.core.rule.engine.api.CoreRuleEngineHelper.mergeInputVariables;

@Slf4j
@Service
class RuleEngine {
    private final CoreRuleEngine coreRuleEngine;
    private final RuleCache ruleCache;

    public RuleEngine(CoreRuleEngine coreRuleEngine, RuleCache ruleCache) {
        this.coreRuleEngine = coreRuleEngine;
        this.ruleCache = ruleCache;
        log.debug("RuleEngine initialized");
    }

    public List<RuleExecutionResult> evaluateMatrices(Map<String, Object> inputVariables, BaseRuleCategory ruleCategory, BaseRuleSubCategory subCategory, HitPolicy hitPolicy) {
        long startTime = System.nanoTime();

        log.debug("Processing with input variables: {}, ruleCategory: {}, subCategory: {}, hitPolicy: {}", inputVariables, ruleCategory, subCategory, hitPolicy);
        List<RuleExecutionResult> results = new ArrayList<>();

        Collection<Rule> filteredRules = ruleCache.findRules(ruleCategory, subCategory);

        for(Rule rule : filteredRules) {
            RuleExecutionResult ruleExecutionResult = evaluate(inputVariables, rule);
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

    private RuleExecutionResult evaluate(Map<String, Object> inputVariables, Rule rule) {
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

    public List<TreeExecutionResult> evaluateTrees(Map<String, Object> inputVariables, BaseRuleCategory ruleCategory, BaseRuleSubCategory subCategory, HitPolicy hitPolicy) {
        long startTime = System.nanoTime();

        log.debug("Evaluating multiple decision trees with input variables: {}", inputVariables);
        List<TreeExecutionResult> results = new ArrayList<>();

        Collection<Rule> filteredRules = ruleCache.findRules(ruleCategory, subCategory);

        for (Rule rule : filteredRules) {
            for (Condition condition : rule.getConditions()) {
                TreeExecutionResult tet = evaluateSingleTree(inputVariables, condition, rule, new ArrayList<>());
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

    private TreeExecutionResult evaluateSingleTree(Map<String, Object> inputVariables,
                                                   Condition condition, Rule rule, List<Condition> executedNodes) {
        executedNodes.add(condition);
        if (condition.isLeaf()) {
            Map<String, Object> results = new HashMap<>();
            Optional<Object> o = coreRuleEngine.executeAction(condition.getAction(), inputVariables);
            o.ifPresent(o1 -> results.put(condition.getAction(), o1));
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

        return evaluateSingleTree(inputVariables, nextCondition, rule, executedNodes);
    }
}