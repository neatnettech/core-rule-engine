package tech.neatnet.core.rule.engine.cache;

import tech.neatnet.rule.engine.domain.Rule;
import tech.neatnet.rule.engine.domain.RuleMatrix;

import java.util.Collection;

public interface RuleCache extends Cache<RuleMatrix>{
    // Retrieve all rules that are stored in the cache
    Collection<RuleMatrix> getAllRules();

    Collection<RuleMatrix> getRulesByCategory(Rule.RuleCategory category);
}
