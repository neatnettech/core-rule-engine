package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "ruleengine.enabled", havingValue = "true", matchIfMissing = true)
@Import(CacheConfiguration.class)
@EnableMongoRepositories(basePackageClasses = RuleRepository.class)
class RuleEngineAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public CoreRuleEngine coreRuleEngine() {
    log.debug("Creating CoreRuleEngine bean");
    return new CoreRuleEngine();
  }

  @Bean
  @ConditionalOnMissingBean
  public RuleEngineClient ruleEngineClient(RuleRepository ruleRepository) {
    log.debug("Creating RuleEngineClient bean");
    return new RuleEngineClient(ruleRepository);
  }

  @Bean
  @ConditionalOnMissingBean
  public RuleCache ruleMatrixCache(RuleRepository ruleRepository) {
    log.debug("Creating RuleCache bean");
    return new RuleCacheImpl(ruleRepository);
  }

  @Bean
  public RuleEngine ruleEngine(CoreRuleEngine coreRuleEngine, RuleCache ruleCache) {
    log.debug("Creating RuleEngine bean");
    return new RuleEngine(coreRuleEngine, ruleCache);
  }
}