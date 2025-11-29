package tech.neatnet.core.rule.engine.api;

import tech.neatnet.core.rule.engine.domain.Rule;
import java.util.Collection;

public interface RuleCache {

    Collection<Rule> findRules(BaseRuleCategory baseRuleCategory, BaseRuleSubCategory baseRuleSubCategory);

    Collection<Rule> findActive();

    void reloadRules();

}
