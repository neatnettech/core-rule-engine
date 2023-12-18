package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.neatnet.core.rule.engine.domain.Condition;
import tech.neatnet.core.rule.engine.domain.Rule;
import tech.neatnet.core.rule.engine.domain.RuleCategory;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class RuleController {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    public Rule saveRule() {

        Rule rule = Rule.builder()
                .id(UUID.randomUUID())
                .name("Sample Matrix")
                .description("This is a sample rule matrix")
                .modifiedBy("user123")
                .version(1)
                .dateCreated(Instant.now())
                .dateModified(Instant.now())
                .conditions(List.of(
                        Condition.builder()
                                .condition("value > 10")
                                .build(),
                        Condition.builder()
                                .condition("value2 == 'test'")
                                .build(),
                        Condition.builder()
                                .condition("value3 in inValues")
                                .inValues(Arrays.asList("test1", "test2"))
                                .build()
                ))
                .results(new HashMap<String, Object>() {{
                    put("signal", "valid");
                }})
                .category(RuleCategory.DUMMY_SIGNAL)
                .build();

        return ruleService.saveRule(rule);
    }


    public void updateRule(Rule ruleMatrix) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
