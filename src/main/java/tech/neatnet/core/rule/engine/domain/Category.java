package tech.neatnet.core.rule.engine.domain;

import tech.neatnet.core.rule.engine.api.BaseRuleCategory;
import tech.neatnet.core.rule.engine.api.BaseRuleSubCategory;

public enum Category implements BaseRuleCategory, BaseRuleSubCategory {
    DUMMY;

    @Override
    public String getName() {
        return name();
    }
}
