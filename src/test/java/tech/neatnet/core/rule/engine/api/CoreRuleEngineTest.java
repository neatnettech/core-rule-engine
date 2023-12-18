package tech.neatnet.core.rule.engine.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.neatnet.core.rule.engine.domain.Condition;
import tech.neatnet.core.rule.engine.domain.Rule;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CoreRuleEngineTest {

    private CoreRuleEngine coreRuleEngine;

    @BeforeEach
    void setUp() {
        coreRuleEngine = new CoreRuleEngine();
    }

    @Test
    @DisplayName("Test evaluateCondition with inValues matching")
    void evaluateInValuesTrue() {

        Rule rule = Rule.builder()
                .conditions(
                        List.of(
                                Condition.builder()
                                        .condition("inValues contains value")
                                        .inValues(Arrays.asList("value1", "value2"))
                                        .build()
                        )
                )
                .build();

        Map<String, Object> context = new HashMap<>();
        context.put("value", "value1");

        assertTrue(coreRuleEngine.evaluateCondition(rule.getConditions().get(0).getCondition(), context,
                Optional.of(rule.getConditions().get(0).getInValues())));
    }

    @Test
    @DisplayName("Test evaluateCondition with inValues not matching")
    void evaluateInValuesFalse() {

        Rule rule = Rule.builder()
                .conditions(
                        List.of(
                                Condition.builder()
                                        .condition("inValues contains value")
                                        .inValues(Arrays.asList("value1", "value2"))
                                        .build()
                        )
                )
                .build();

        Map<String, Object> context = new HashMap<>();
        context.put("value", "value3");

        assertFalse(coreRuleEngine.evaluateCondition(rule.getConditions().get(0).getCondition(), context,
                Optional.of(rule.getConditions().get(0).getInValues())));
    }

    @Test
    @DisplayName("Test evaluateCondition with true condition")
    void evaluateConditionTrue() {

        Rule rule = Rule.builder()
                .conditions(
                        List.of(
                                Condition.builder()
                                        .condition("value == true")
                                        .build()
                        )
                )
                .build();

        Map<String, Object> context = new HashMap<>();
        context.put("value", true);

        assertTrue(coreRuleEngine.evaluateCondition(rule.getConditions().get(0).getCondition(), context,
                Optional.empty()));
    }

    @Test
    @DisplayName("Test evaluateCondition with false condition")
    void evaluateConditionFalse() {

        Rule rule = Rule.builder()
                .conditions(
                        List.of(
                                Condition.builder()
                                        .condition("value == false")
                                        .build()
                        )
                )
                .build();

        Map<String, Object> context = new HashMap<>();
        context.put("value", true);
        assertFalse(coreRuleEngine.evaluateCondition(rule.getConditions().get(0).getCondition(), context,
                Optional.empty()));
    }

    @Test
    @DisplayName("Test evaluateCondition with action")
    void executeActionSuccess() {
        String action = "result = 'success'";
        Map<String, Object> data = new HashMap<>();
        Optional<Object> result = coreRuleEngine.executeAction(action, data);
        assertTrue(result.isPresent());
        assertEquals("success", result.get());
    }

    @Test
    void executeActionException() {
        String action = "invalid expression";
        Map<String, Object> data = new HashMap<>();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            coreRuleEngine.executeAction(action, data);
        });

        String expectedMessage = "Failed to execute action";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }


    @Test
    @DisplayName("Test evaluateCondition with multiple rules")
    void evaluateConditionMultipleRules() {

        Rule ruleMultipleConditions = Rule.builder()
                .conditions(
                        List.of(
                                Condition.builder()
                                        .condition("value1 > 10")
                                        .build(),
                                Condition.builder()
                                        .condition("value2 == 'test'")
                                        .build(),
                                Condition.builder()
                                        .condition("inValues contains value3")
                                        .inValues(Arrays.asList("test1", "test2"))
                                        .build()
                        )
                )
                .build();


        Map<String, Object> context = new HashMap<>();
        context.put("value1", 11);
        context.put("value2", "test");
        context.put("value3", "test1");

        assertTrue(coreRuleEngine.evaluateCondition(ruleMultipleConditions.getConditions().get(0).getCondition(), context, Optional.empty()));
        assertTrue(coreRuleEngine.evaluateCondition(ruleMultipleConditions.getConditions().get(1).getCondition(), context, Optional.empty()));
        assertTrue(coreRuleEngine.evaluateCondition(ruleMultipleConditions.getConditions().get(2).getCondition(), context, Optional.of(ruleMultipleConditions.getConditions().get(2).getInValues())));

    }

}
