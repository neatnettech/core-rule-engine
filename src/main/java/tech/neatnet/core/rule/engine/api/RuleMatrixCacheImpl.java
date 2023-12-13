package tech.neatnet.core.rule.engine.api;

import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import tech.neatnet.core.rule.engine.domain.RuleMatrix;

@Slf4j
@Component
class RuleMatrixCacheImpl implements RuleMatrixCache {

  private final RuleMatrixRepository ruleMatrixRepository;

  public RuleMatrixCacheImpl(RuleMatrixRepository ruleMatrixRepository) {
    this.ruleMatrixRepository = ruleMatrixRepository;
  }

  @Override
  @Cacheable(value = "ruleMatrices", key = "'allRuleMatrices'")
  public Collection<RuleMatrix> getAllRuleMatrices() {
    log.info("Loading all RuleMatrices from DB");
    return ruleMatrixRepository.findAll();
  }

  @Override
  @CacheEvict(value = "ruleMatrices", allEntries = true)
  public void reloadRuleMatrices() {
    log.info("Reloading all RuleMatrices from DB");
    getAllRuleMatrices();
  }

}
