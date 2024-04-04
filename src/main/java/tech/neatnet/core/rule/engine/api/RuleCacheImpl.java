package tech.neatnet.core.rule.engine.api;


import lombok.extern.slf4j.Slf4j;

import tech.neatnet.core.rule.engine.domain.Rule;

import java.util.Collection;

import org.springframework.stereotype.Component;

@Slf4j
@Component
class RuleCacheImpl implements RuleCache {

    private final RuleRepositoryService ruleRepositoryService;

    public RuleCacheImpl(RuleRepositoryService ruleRepositoryService) {
        log.debug("RuleCacheImpl constructor called");
        this.ruleRepositoryService = ruleRepositoryService;
    }

    // Method to get rules based on dynamic filtering criteria
    @Override
    public Collection<Rule> findRules(BaseRuleCategory baseRuleCategory, BaseRuleSubCategory baseRuleSubCategory) {
        return ruleRepositoryService.findRulesByBaseRuleCategoryAndBaseRuleSubCategory(baseRuleCategory, baseRuleSubCategory);
    }


    // This method may remain the same if you still need a way to load all rules unfiltered.
    @Override
    public Collection<Rule> findActive() {
        return ruleRepositoryService.findRulesByActive(true);
    }

    @Override
    public void reloadRules() {
        findActive();
    }
}