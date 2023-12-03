package tech.neatnet.core.rule.engine.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import tech.neatnet.core.rule.engine.api.RuleEngine;
import tech.neatnet.core.rule.engine.cache.RuleMatrixCache;
import tech.neatnet.core.rule.engine.cache.RuleMatrixCacheImpl;
import tech.neatnet.core.rule.engine.core.CoreRuleEngine;
import tech.neatnet.core.rule.engine.repositories.RuleMatrixRepository;
import tech.neatnet.core.rule.engine.service.RuleMatrixService;

@Configuration
@ConditionalOnProperty(name = "ruleengine.enabled", havingValue = "true", matchIfMissing = true)
@EnableMongoRepositories(basePackageClasses = RuleMatrixRepository.class)
public class RuleEngineAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public RuleMatrixService ruleMatrixService(RuleMatrixRepository ruleMatrixRepository) {
    return new RuleMatrixService(ruleMatrixRepository);
  }

  @Bean
  @ConditionalOnMissingBean
  public CoreRuleEngine coreRuleEngine() {
    return new CoreRuleEngine();
  }

  @Bean
  @ConditionalOnMissingBean
  public RuleMatrixCache ruleMatrixCache(RuleMatrixRepository ruleMatrixRepository) {
    return new RuleMatrixCacheImpl(ruleMatrixRepository);
  }

  @Bean
  public RuleEngine ruleEngineAPI(CoreRuleEngine coreRuleEngine, RuleMatrixCache ruleMatrixCache,
      RuleMatrixService ruleMatrixService) {
    return new RuleEngine(coreRuleEngine, ruleMatrixCache, ruleMatrixService);
  }
}
