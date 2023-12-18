package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import tech.neatnet.core.rule.engine.domain.Rule;

import java.util.Collection;

@Slf4j
@Component
class RuleCacheImpl implements RuleCache {

    private final RuleRepository ruleRepository;

    public RuleCacheImpl(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    @Cacheable(value = "rules", key = "'allRules'")
    public Collection<Rule> getAllRules() {
        log.info("Loading all Rules from DB");
        return ruleRepository.findAll();
    }

    @Override
    @CacheEvict(value = "rules", allEntries = true)
    public void reloadRules() {
        log.info("Reloading all Rules from DB");
        getAllRules();
    }

}
