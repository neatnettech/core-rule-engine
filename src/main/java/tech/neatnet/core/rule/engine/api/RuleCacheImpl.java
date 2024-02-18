package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import tech.neatnet.core.rule.engine.domain.Rule;
import tech.neatnet.core.rule.engine.domain.RuleType;

import java.util.Collection;

@Slf4j
@Component
class RuleCacheImpl implements RuleCache {

    private final RuleRepository ruleRepository;

    public RuleCacheImpl(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    @Cacheable(value = "ruleMatrices", key = "'allMatrices'")
    public Collection<Rule> getAllMatrices() {
        log.debug("Loading all matrices from DB");
        return ruleRepository.getRulesByRuleType(RuleType.MATRIX);
    }

    @Override
    @CacheEvict(value = "ruleMatrices", allEntries = true)
    public void reloadMatrices() {
        log.debug("Reloading all Rules from DB");
        getAllMatrices();
    }

    @Override
    @Cacheable(value = "ruleTrees", key = "'allTrees'")
    public Collection<Rule> getAllTrees() {
        log.debug("Loading all Decision Trees from DB");
        return ruleRepository.getRulesByRuleType(RuleType.TREE);
    }

    @Override
    @CacheEvict(value = "ruleTrees", allEntries = true)
    public void reloadTrees() {
        log.debug("Reloading all Decision Trees from DB");
        getAllTrees();
    }

}
