package tech.neatnet.core.rule.engine.domain;

import tech.neatnet.core.rule.engine.api.BaseRuleCategory;
import tech.neatnet.core.rule.engine.api.BaseRuleSubCategory;

/**
 * Default category implementation for quick start.
 * <p>
 * For production use, create your own enum implementing
 * {@link BaseRuleCategory} and {@link BaseRuleSubCategory}.
 * <p>
 * Example:
 * <pre>
 * public enum MyCategory implements BaseRuleCategory {
 *     PRICING, VALIDATION, WORKFLOW;
 *
 *     public String getName() { return name(); }
 * }
 * </pre>
 */
public enum Category implements BaseRuleCategory, BaseRuleSubCategory {

    DEFAULT,
    GENERAL,
    VALIDATION,
    PRICING,
    WORKFLOW;

    @Override
    public String getName() {
        return name();
    }
}
