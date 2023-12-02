package tech.neatnet.core.rule.engine.cache;

import java.util.Collection;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import tech.neatnet.core.rule.engine.domain.RuleMatrix;

public interface RuleMatrixCache {

  @Cacheable(value = "ruleMatrices")
  Collection<RuleMatrix> getAllRuleMatrices();

  @Scheduled(fixedDelay = 60000)
  @CacheEvict(value = "ruleMatrices", allEntries = true)
  void reloadRuleMatrices();
}
