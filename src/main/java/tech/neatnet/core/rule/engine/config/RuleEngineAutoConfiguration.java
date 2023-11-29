package tech.neatnet.core.rule.engine.config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import tech.neatnet.core.rule.engine.api.RuleEngineAPI;
import tech.neatnet.core.rule.engine.cache.RuleMatrixCache;
import tech.neatnet.core.rule.engine.cache.RuleMatrixCacheImpl;
import tech.neatnet.core.rule.engine.core.CoreRuleEngine;
import tech.neatnet.core.rule.engine.repositories.RuleMatrixRepository;

@Configuration
@ConditionalOnProperty(name = "ruleengine.enabled", havingValue = "true", matchIfMissing = true)
@EnableMongoRepositories(basePackageClasses = RuleMatrixRepository.class)
public class RuleEngineAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CoreRuleEngine coreRuleEngine() {
        return new CoreRuleEngine();
    }

    @Bean
    @ConditionalOnMissingBean
    public RuleMatrixCache ruleMatrixCache(RuleMatrixRepository ruleMatrixRepository) {
        // Set up and return RuleMatrixCache
        return new RuleMatrixCacheImpl(ruleMatrixRepository);
    }

    @Bean
    public RuleEngineAPI ruleEngineAPI(CoreRuleEngine coreRuleEngine, RuleMatrixCache ruleMatrixCache) {
        return new RuleEngineAPI(coreRuleEngine, ruleMatrixCache);
    }
}
