package tech.neatnet.core.rule.engine.api;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import tech.neatnet.core.rule.engine.domain.Rule;

import java.util.Collection;

public interface RuleCache {

    @Cacheable(value = "rules")
    Collection<Rule> getAllRules();

    @Scheduled(fixedDelay = 10000)
    @CacheEvict(value = "rules", allEntries = true)
    void reloadRules();

}
