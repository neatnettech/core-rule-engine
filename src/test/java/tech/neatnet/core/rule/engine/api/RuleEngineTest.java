package tech.neatnet.core.rule.engine.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.neatnet.core.rule.engine.domain.Condition;
import tech.neatnet.core.rule.engine.domain.Rule;
import tech.neatnet.core.rule.engine.domain.RuleExecutionResult;
import tech.neatnet.core.rule.engine.domain.TreeExecutionResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleEngineTest {

    @Mock
    CoreRuleEngine coreRuleEngine;

    @Mock
    RuleCache ruleCache;

    @InjectMocks
    RuleEngine ruleEngine;


    @Test
    void testEvaluateRules() {
        // Arrange
        Map<String, Object> inputVariables = new HashMap<>();
        Rule rule = mock(Rule.class);
        when(ruleCache.getAllMatrices()).thenReturn(Collections.singletonList(rule));
        when(coreRuleEngine.evaluateCondition(any(), any())).thenReturn(true);
        when(rule.getResults()).thenReturn(new HashMap<>());

        // Act
        List<RuleExecutionResult> results = ruleEngine.evaluateRules(inputVariables);

        // Assert
        assertFalse(results.isEmpty());
        verify(coreRuleEngine, times(1)).evaluateCondition(any(), any());
    }

    @Test
    void testEvaluateMultipleDecisionTrees() {
        // Arrange
        Map<String, Object> inputVariables = new HashMap<>();
        Rule rule = mock(Rule.class);
        Condition condition = mock(Condition.class);
        when(condition.isLeaf()).thenReturn(true);
        when(rule.getConditions()).thenReturn(Collections.singletonList(condition));
        when(ruleCache.getAllTrees()).thenReturn(Collections.singletonList(rule));

        // Act
        List<TreeExecutionResult> results = ruleEngine.evaluateMultipleDecisionTrees(inputVariables);

        // Assert
        assertFalse(results.isEmpty());
    }
}