package tech.neatnet.core.rule.engine.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.neatnet.core.rule.engine.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RuleEngine Tests")
class RuleEngineTest {

    private CoreRuleEngine coreRuleEngine;

    @Mock
    private RuleCache ruleCache;

    private RuleEngine ruleEngine;

    @BeforeEach
    void setUp() {
        // Use real CoreRuleEngine since it's lightweight and doesn't need mocking
        coreRuleEngine = new CoreRuleEngine(100);
        ruleEngine = new RuleEngine(coreRuleEngine, ruleCache);
    }

    @Nested
    @DisplayName("evaluateMatrices - Decision Tables")
    class EvaluateMatrices {

        @Test
        @DisplayName("should return matched result when all conditions are true")
        void shouldReturnMatchedResultWhenAllConditionsTrue() {
            Map<String, Object> input = new HashMap<>();
            input.put("value", 100);
            Rule rule = createDecisionTableRule("Test Rule",
                    List.of(Condition.builder().condition("value > 50").build()),
                    Map.of("discount", 10));

            when(ruleCache.findRules(any(), any())).thenReturn(List.of(rule));

            List<RuleExecutionResult> results = ruleEngine.evaluateMatrices(
                    input, Category.DEFAULT, Category.DEFAULT, HitPolicy.FIRST);

            assertEquals(1, results.size());
            assertTrue(results.get(0).isRuleCriteriaMet());
            assertEquals(10, results.get(0).getResults().get("discount"));
        }

        @Test
        @DisplayName("should return unmatched result when condition is false")
        void shouldReturnUnmatchedResultWhenConditionFalse() {
            Map<String, Object> input = new HashMap<>();
            input.put("value", 30);
            Rule rule = createDecisionTableRule("Test Rule",
                    List.of(Condition.builder().condition("value > 50").build()),
                    Map.of("discount", 10));

            when(ruleCache.findRules(any(), any())).thenReturn(List.of(rule));

            List<RuleExecutionResult> results = ruleEngine.evaluateMatrices(
                    input, Category.DEFAULT, Category.DEFAULT, HitPolicy.FIRST);

            assertEquals(1, results.size());
            assertFalse(results.get(0).isRuleCriteriaMet());
        }

        @Test
        @DisplayName("should stop at first match with FIRST hit policy")
        void shouldStopAtFirstMatchWithFirstHitPolicy() {
            Map<String, Object> input = new HashMap<>();
            input.put("value", 100);
            Rule rule1 = createDecisionTableRule("Rule 1",
                    List.of(Condition.builder().condition("value > 50").build()),
                    Map.of("result", "first"));
            Rule rule2 = createDecisionTableRule("Rule 2",
                    List.of(Condition.builder().condition("value > 50").build()),
                    Map.of("result", "second"));

            when(ruleCache.findRules(any(), any())).thenReturn(List.of(rule1, rule2));

            List<RuleExecutionResult> results = ruleEngine.evaluateMatrices(
                    input, Category.DEFAULT, Category.DEFAULT, HitPolicy.FIRST);

            assertEquals(1, results.size());
            assertEquals("first", results.get(0).getResults().get("result"));
        }

        @Test
        @DisplayName("should evaluate all rules with COLLECT hit policy")
        void shouldEvaluateAllRulesWithCollectHitPolicy() {
            Map<String, Object> input = new HashMap<>();
            input.put("value", 100);
            Rule rule1 = createDecisionTableRule("Rule 1",
                    List.of(Condition.builder().condition("value > 50").build()),
                    Map.of("result", "first"));
            Rule rule2 = createDecisionTableRule("Rule 2",
                    List.of(Condition.builder().condition("value > 50").build()),
                    Map.of("result", "second"));

            when(ruleCache.findRules(any(), any())).thenReturn(List.of(rule1, rule2));

            List<RuleExecutionResult> results = ruleEngine.evaluateMatrices(
                    input, Category.DEFAULT, Category.DEFAULT, HitPolicy.COLLECT);

            assertEquals(2, results.size());
            assertTrue(results.stream().allMatch(RuleExecutionResult::isRuleCriteriaMet));
        }

        @Test
        @DisplayName("should return empty results when no rules found")
        void shouldReturnEmptyResultsWhenNoRulesFound() {
            when(ruleCache.findRules(any(), any())).thenReturn(Collections.emptyList());

            List<RuleExecutionResult> results = ruleEngine.evaluateMatrices(
                    Map.of(), Category.DEFAULT, Category.DEFAULT, HitPolicy.FIRST);

            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should handle rule with empty conditions")
        void shouldHandleRuleWithEmptyConditions() {
            Rule rule = createDecisionTableRule("Empty Rule",
                    Collections.emptyList(),
                    Map.of("result", "matched"));

            when(ruleCache.findRules(any(), any())).thenReturn(List.of(rule));

            List<RuleExecutionResult> results = ruleEngine.evaluateMatrices(
                    Map.of(), Category.DEFAULT, Category.DEFAULT, HitPolicy.FIRST);

            assertEquals(1, results.size());
            assertTrue(results.get(0).isRuleCriteriaMet());
        }
    }

    @Nested
    @DisplayName("evaluateTrees - Decision Trees")
    class EvaluateTrees {

        @Test
        @DisplayName("should traverse tree and execute leaf action")
        void shouldTraverseTreeAndExecuteLeafAction() {
            Condition leaf = Condition.builder().action("'APPROVED'").build();
            Condition root = Condition.builder()
                    .condition("age >= 21")
                    .trueBranch(leaf)
                    .falseBranch(Condition.builder().action("'REJECTED'").build())
                    .build();

            Rule rule = Rule.builder()
                    .name("Tree Rule")
                    .ruleType(RuleType.DECISION_TREE)
                    .conditions(List.of(root))
                    .active(true)
                    .build();

            when(ruleCache.findRules(any(), any())).thenReturn(List.of(rule));

            Map<String, Object> input = new HashMap<>();
            input.put("age", 25);

            List<TreeExecutionResult> results = ruleEngine.evaluateTrees(
                    input, Category.DEFAULT, Category.DEFAULT, HitPolicy.FIRST);

            assertEquals(1, results.size());
            assertTrue(results.get(0).isRuleCriteriaMet());
        }

        @Test
        @DisplayName("should follow false branch when condition is false")
        void shouldFollowFalseBranchWhenConditionFalse() {
            Condition root = Condition.builder()
                    .condition("age >= 21")
                    .trueBranch(Condition.builder().action("'APPROVED'").build())
                    .falseBranch(Condition.builder().action("'REJECTED'").build())
                    .build();

            Rule rule = Rule.builder()
                    .name("Tree Rule")
                    .ruleType(RuleType.DECISION_TREE)
                    .conditions(List.of(root))
                    .active(true)
                    .build();

            when(ruleCache.findRules(any(), any())).thenReturn(List.of(rule));

            Map<String, Object> input = new HashMap<>();
            input.put("age", 18);

            List<TreeExecutionResult> results = ruleEngine.evaluateTrees(
                    input, Category.DEFAULT, Category.DEFAULT, HitPolicy.FIRST);

            assertEquals(1, results.size());
        }
    }

    private Rule createDecisionTableRule(String name, List<Condition> conditions, Map<String, Object> results) {
        return Rule.builder()
                .name(name)
                .ruleType(RuleType.DECISION_TABLE)
                .conditions(conditions)
                .results(results)
                .active(true)
                .build();
    }
}
