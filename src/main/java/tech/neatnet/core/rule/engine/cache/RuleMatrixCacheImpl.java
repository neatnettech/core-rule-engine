package tech.neatnet.core.rule.engine.cache;

import java.util.Collection;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import tech.neatnet.core.rule.engine.domain.RuleMatrix;
import tech.neatnet.core.rule.engine.repositories.RuleMatrixRepository;

@Component
public class RuleMatrixCacheImpl implements RuleMatrixCache {

  private final RuleMatrixRepository ruleMatrixRepository;

  public RuleMatrixCacheImpl(RuleMatrixRepository ruleMatrixRepository) {
    this.ruleMatrixRepository = ruleMatrixRepository;
  }

  @Override
  @Cacheable(value = "ruleMatrices")
  public Collection<RuleMatrix> getAllRuleMatrices() {
    return ruleMatrixRepository.findAll();
  }

  @Override
  @CacheEvict(value = "ruleMatrices", allEntries = true)
  public void reloadRuleMatrices() {
  }

}
