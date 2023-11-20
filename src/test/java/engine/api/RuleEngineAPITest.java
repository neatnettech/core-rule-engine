package engine.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import tech.neatnet.core.rule.engine.api.RuleEngineAPI;
import tech.neatnet.rule.engine.cache.RuleCache;
import tech.neatnet.rule.engine.core.CoreRuleEngine;
import tech.neatnet.rule.engine.domain.Rule;
import tech.neatnet.rule.engine.domain.RuleExecutionResult;
import tech.neatnet.rule.engine.service.RuleRepositoryService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class RuleEngineAPITest {

    @Mock
    private CoreRuleEngine coreRuleEngine;

    @Mock
    private RuleCache ruleCache;

    @Mock
    private RuleRepositoryService ruleRepositoryService;

    @InjectMocks
    private RuleEngineAPI ruleEngineAPI;

    @Test
    void whenEvaluateAllRules_thenSuccess() {
        // Arrange
        Map<String, Object> inputVariables = new HashMap<>();
        inputVariables.put("inputKey", "inputValue");


        Rule rule1 = new Rule(UUID.randomUUID(), 1, Rule.RuleCategory.LARGE, "data.value > 100", "result.value = true", null);
        Rule rule2 = new Rule(UUID.randomUUID(), 1, Rule.RuleCategory.LARGE, "data.value1 > 200", "result.value1 = true", null);

        List<Rule> rules = List.of(rule1, rule2);

        when(ruleCache.getAllRules()).thenReturn(rules);
        when(coreRuleEngine.evaluateCondition(anyString(), anyMap())).thenReturn(true);
        when(coreRuleEngine.executeAction(anyString(), anyMap())).thenReturn(Optional.of(Collections.singletonMap("result.value", "true")));

        // Act
        List<RuleExecutionResult> results = ruleEngineAPI.evaluateAllRules(inputVariables);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(coreRuleEngine, times(2)).evaluateCondition(anyString(), anyMap());
        verify(coreRuleEngine, times(2)).executeAction(anyString(), anyMap());

        RuleExecutionResult result1 = results.get(0);
        assertEquals("result.value = true", result1.getRule().getAction());
        assertEquals(Collections.singletonMap("result.value", "true"), result1.getResults());
    }

    @Test
    void whenEvaluateAllRules_andConditionFails_thenActionNotExecuted() {
        // Arrange
        Map<String, Object> inputVariables = new HashMap<>();
        inputVariables.put("inputKey", "inputValue");

        Rule rule = new Rule(UUID.randomUUID(), 1, Rule.RuleCategory.ANOMALY, "data.value >= 100", "result.value = true", null);
        List<Rule> rules = Collections.singletonList(rule);

        when(ruleCache.getAllRules()).thenReturn(rules);
        when(coreRuleEngine.evaluateCondition(anyString(), anyMap())).thenReturn(false);

        // Act
        List<RuleExecutionResult> results = ruleEngineAPI.evaluateAllRules(inputVariables);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(coreRuleEngine, times(1)).evaluateCondition(anyString(), anyMap());
        verify(coreRuleEngine, never()).executeAction(anyString(), anyMap());

        RuleExecutionResult result = results.get(0);
        assertNull(result.getResults().get("actionResult"));
    }
}
