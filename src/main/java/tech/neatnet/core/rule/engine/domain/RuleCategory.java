package tech.neatnet.core.rule.engine.domain;

import tech.neatnet.core.rule.engine.api.BaseRuleCategory;

public enum RuleCategory implements BaseRuleCategory {

    DEFAULT;

    @Override
    public String getName() {
        return name();
    }
}