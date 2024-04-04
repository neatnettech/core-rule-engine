package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import tech.neatnet.core.rule.engine.domain.Rule;

import java.util.Collection;

@Slf4j
@Component
public class RuleRepositoryService {

    private final RuleRepository ruleRepository;

    public RuleRepositoryService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Cacheable(value = "rules", keyGenerator = "ruleKeyGenerator")
    public Collection<Rule> findRulesByBaseRuleCategoryAndBaseRuleSubCategory(BaseRuleCategory baseRuleCategory, BaseRuleSubCategory baseRuleSubCategory) {
        log.debug("Loading rules from DB for category: {}, subcategory: {}", baseRuleCategory, baseRuleSubCategory);
        return ruleRepository.findRulesByBaseRuleCategoryAndBaseRuleSubCategory(baseRuleCategory, baseRuleSubCategory);
    }

    @Cacheable(value = "rules", key = "'allRules'")
    public Collection<Rule> findRulesByActive(boolean active) {
        log.debug("Loading all rules from DB");
        return ruleRepository.findRulesByActive(active);
    }

    @CacheEvict(value = "rules", allEntries = true)
    public void reloadRules() {
        log.debug("Reloading all rules from DB");
        findRulesByActive(true);
    }
}
