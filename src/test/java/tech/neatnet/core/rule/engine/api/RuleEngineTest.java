package tech.neatnet.core.rule.engine.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.neatnet.core.rule.engine.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleEngineTest {

    @Mock
    private CoreRuleEngine coreRuleEngine;

    @Mock
    private RuleCache ruleCache;

    @Mock
    private RuleService ruleService;

    private RuleEngine ruleEngine;

    @BeforeEach
    void setUp() {
        ruleEngine = new RuleEngine(coreRuleEngine, ruleCache, ruleService);
    }

    @Test
    @DisplayName("Test evaluateAllRules with no rules")
    void testEvaluateAllRulesWithNoRules() {
        when(ruleCache.getAllRules()).thenReturn(Collections.emptyList());

        Optional<List<RuleExecutionResult>> results = ruleEngine.evaluateDecisionMatrices(new HashMap<>());

        assertTrue(results.isPresent());
        assertTrue(results.get().isEmpty());
    }


    @Test
    @DisplayName("Test evaluateAllRules with true condition")
    void testEvaluateAllRulesWithTrueCondition() {
        // Create a rule as per the test requirements
        Rule rule = Rule.builder()
                .conditions(Collections.singletonList(Condition.builder()
                        .condition("value1 == 'ABC'")
                        .build()))
                .build();

        // Define a non-empty resultMap for the RuleMatrix
        Map<String, Object> resultMap = Map.of("someKey", "someValue");

        // Mocking behavior of dependencies
        when(ruleCache.getAllRules()).thenReturn(Collections.singletonList(rule));
        when(coreRuleEngine.evaluateCondition(anyString(), anyMap(), any())).thenReturn(true);

        // Evaluate all rules
        Optional<List<RuleExecutionResult>> results = ruleEngine.evaluateDecisionMatrices(new HashMap<>());

        // Assertions
        assertTrue(results.isPresent());
        assertFalse(results.get().isEmpty());
        assertEquals(1, results.get().size());
        assertFalse(results.get().get(0).getResults().isEmpty());
        assertEquals(resultMap, results.get().get(0).getResults());
    }


    @Test
    @DisplayName("Test evaluateAllRules with false condition")
    void testEvaluateAllRulesWithFalseCondition() {
        Rule rule = Rule.builder().conditions(
                List.of(
                        Condition.builder()
                                .condition("condition")
                                .build()
                )
        ).build();

        when(ruleCache.getAllRules()).thenReturn(Collections.singletonList(rule));
        when(coreRuleEngine.evaluateCondition(anyString(), anyMap(), any())).thenReturn(false);

        Optional<List<RuleExecutionResult>> results = ruleEngine.evaluateDecisionMatrices(new HashMap<>());

        assertTrue(results.isPresent());
        assertEquals(1, results.get().size());
        assertTrue(results.get().get(0).getResults().isEmpty());
    }


    @Test
    @DisplayName("Test evaluateAllRules with complex inputVariables")
    void testEvaluateAllRulesWithComplexInputVariables() {
        Rule rule = Rule.builder()
                .conditions(List.of(Condition.builder()
                        .condition("value1 == 'ABC'")
                        .build()
                        ,
                        Condition.builder()
                                .condition("value2 > 100")
                                .build()
                        ,
                        Condition.builder()
                                .condition("value3 in inValues")
                                .inValues(List.of("1", "2", "3"))
                                .build()
                        )

                )
                .build();

        when(ruleCache.getAllRules()).thenReturn(Collections.singletonList(rule));
        when(coreRuleEngine.evaluateCondition(anyString(), anyMap(), any())).thenReturn(true);

        Map<String, Object> inputVariables = new HashMap<>();
        inputVariables.put("value1", "ABC");
        inputVariables.put("value2", 101);
        inputVariables.put("value3", "2");

        Optional<List<RuleExecutionResult>> results = ruleEngine.evaluateDecisionMatrices(inputVariables);

        assertTrue(results.isPresent());
        assertFalse(results.get().isEmpty());
        assertEquals(Map.of("hit", true), results.get().get(0).getResults());
    }


    @Test
    @DisplayName("Test evaluateAllRules with hit result assigned on Matrix level")
    void testEvaluateAllRulesWithHitResult() {
        // Create rules as before
        Rule rule = Rule.builder().conditions(
                List.of(
                        Condition.builder()
                                .condition("value1 == 'ABC'")
                                .build(),
                        Condition.builder()
                                .condition("value2 > 100")
                                .build(),
                        Condition.builder()
                                .condition("value3 in inValues")
                                .inValues(List.of("1", "2", "3"))
                                .build()
                )
        ).build();

        // Define the resultMap for the RuleMatrix
        Map<String, Object> resultMap = Map.of("hit", true);

        // Mocking behavior of dependencies
        when(ruleCache.getAllRules()).thenReturn(Collections.singletonList(rule));
        when(coreRuleEngine.evaluateCondition(anyString(), anyMap(), any())).thenReturn(true);

        // Define input variables
        Map<String, Object> inputVariables = new HashMap<>();
        inputVariables.put("value1", "ABC");
        inputVariables.put("value2", 101);
        inputVariables.put("value3", "2");

        // Evaluate all rules
        Optional<List<RuleExecutionResult>> optionalResults = ruleEngine.evaluateDecisionMatrices(
                inputVariables);

        // Assertions
        assertTrue(optionalResults.isPresent());
        List<RuleExecutionResult> results = optionalResults.get();
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(resultMap, results.get(0).getResults());
    }

}

