package tech.neatnet.core.rule.engine.api;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import tech.neatnet.core.rule.engine.domain.Rule;

import java.util.Collection;

public interface RuleCache {

    @Cacheable(value = "ruleMatrices")
    Collection<Rule> getAllRules();

    @Scheduled(fixedDelay = 60000)
    @CacheEvict(value = "ruleMatrices", allEntries = true)
    void reloadRules();
}
