package tech.neatnet.core.rule.engine.domain;

import tech.neatnet.core.rule.engine.BaseRuleCategory;

public enum RuleCategory implements BaseRuleCategory {

    SIGNAL;

    @Override
    public String getName() {
        return name();
    }
}