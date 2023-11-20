package tech.neatnet.core.rule.engine.cache;

import org.springframework.stereotype.Component;
import tech.neatnet.rule.engine.domain.Rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class InMemoryRuleCache implements RuleCache {
    private final List<Rule> rules = Collections.synchronizedList(new ArrayList<Rule>());

    @Override
    public Collection<Rule> getAllRules() {
        return this.rules;
    }

    @Override
    public Collection<Rule> getRulesByCategory(Rule.RuleCategory category) {
        return this.rules.stream()
                .filter(rule -> rule.getCategory().equals(category.name()))
                .toList();
    }

}
