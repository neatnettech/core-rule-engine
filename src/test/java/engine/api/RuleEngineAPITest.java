package engine.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.neatnet.core.rule.engine.api.RuleEngineAPI;
import tech.neatnet.core.rule.engine.core.CoreRuleEngine;
import tech.neatnet.core.rule.engine.service.RuleRepositoryService;


@ExtendWith(MockitoExtension.class)
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

    }

    @Test
    void whenEvaluateAllRules_andConditionFails_thenActionNotExecuted() {

    }
}
