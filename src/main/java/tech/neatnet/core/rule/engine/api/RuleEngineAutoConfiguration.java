package tech.neatnet.core.rule.engine.api;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@ConditionalOnProperty(name = "ruleengine.enabled", havingValue = "true", matchIfMissing = true)
@Import(CacheConfiguration.class)
@EnableMongoRepositories(basePackageClasses = RuleMatrixRepository.class)
class RuleEngineAutoConfiguration {

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
