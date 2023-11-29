package tech.neatnet.core.rule.engine.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import tech.neatnet.core.rule.engine.domain.RuleMatrix;

import java.util.Collection;

public interface RuleMatrixCache {

    @Cacheable(value = "ruleMatrices")
    Collection<RuleMatrix> getAllRuleMatrices();

    @CacheEvict(value = "ruleMatrices", allEntries = true)
    void reloadRuleMatrices();
}
