package tech.neatnet.core.rule.engine.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.neatnet.core.rule.engine.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RuleEngineTest {

    @Mock
    private CoreRuleEngine coreRuleEngine;

    @Mock
    private RuleCache ruleCache;

    @InjectMocks
    private RuleEngine ruleEngine;


    @Test
    public void testEvaluateMatrices() {
        Map<String, Object> inputVariables = new HashMap<>();
        BaseRuleCategory ruleCategory = Category.DUMMY;
        BaseRuleSubCategory subCategory = Category.DUMMY;
        HitPolicy hitPolicy = HitPolicy.FIRST;

        Rule rule = new Rule();
        rule.setConditions(Collections.emptyList());
        rule.setResults(new HashMap<>());

        when(ruleCache.findRules(ruleCategory, subCategory)).thenReturn(Collections.singletonList(rule));
        when(coreRuleEngine.evaluateCondition(anyString(), anyMap())).thenReturn(true);

        List<RuleExecutionResult> results = ruleEngine.evaluateMatrices(inputVariables, ruleCategory, subCategory, hitPolicy);

        assertEquals(1, results.size());
        assertEquals(true, results.get(0).isRuleCriteriaMet());
    }

    // Add more tests here for other methods in RuleEngine class
}