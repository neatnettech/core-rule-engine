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
@EnableMongoRepositories(basePackageClasses = RuleRepository.class)
class RuleEngineAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public RuleService ruleMatrixService(RuleRepository ruleRepository) {
    return new RuleService(ruleRepository);
  }

  @Bean
  @ConditionalOnMissingBean
  public CoreRuleEngine coreRuleEngine() {
    return new CoreRuleEngine();
  }

  @Bean
  @ConditionalOnMissingBean
  public RuleCache ruleMatrixCache(RuleRepository ruleRepository) {
    return new RuleCacheImpl(ruleRepository);
  }

  @Bean
  public RuleEngine ruleEngineAPI(CoreRuleEngine coreRuleEngine, RuleCache ruleCache,
      RuleService ruleService) {
    return new RuleEngine(coreRuleEngine, ruleCache, ruleService);
  }
}
