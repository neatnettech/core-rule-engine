package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto-configuration for the Rule Engine.
 * <p>
 * Just add the dependency - everything auto-configures with sensible defaults:
 * <ul>
 *     <li>MongoDB: localhost:27017/rule-engine</li>
 *     <li>Cache: "rules" cache with 1000 heap entries</li>
 * </ul>
 * <p>
 * Disable with: {@code rule.engine.enabled=false}
 *
 * @see RuleEngineProperties
 * @see RuleEngineClient
 */
@Slf4j
@AutoConfiguration(before = MongoDataAutoConfiguration.class)
@ConditionalOnProperty(name = "rule.engine.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RuleEngineProperties.class)
@EnableMongoRepositories(basePackageClasses = RuleRepository.class)
@Import(CacheConfiguration.class)
public class RuleEngineAutoConfiguration {

    public RuleEngineAutoConfiguration() {
        log.info("Rule Engine auto-configuration initialized");
    }

    @Bean
    @ConditionalOnMissingBean
    public CoreRuleEngine coreRuleEngine(RuleEngineProperties properties) {
        int maxCacheSize = properties.getExpression().getMaxCacheSize();
        return new CoreRuleEngine(maxCacheSize);
    }

    @Bean
    @ConditionalOnMissingBean
    public RuleRepositoryService ruleRepositoryService(RuleRepository ruleRepository) {
        return new RuleRepositoryService(ruleRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public RuleCache ruleCache(RuleRepositoryService ruleRepositoryService) {
        return new RuleCacheImpl(ruleRepositoryService);
    }

    @Bean
    @ConditionalOnMissingBean
    public RuleEngine ruleEngine(CoreRuleEngine coreRuleEngine, RuleCache ruleCache) {
        return new RuleEngine(coreRuleEngine, ruleCache);
    }

    @Bean
    @ConditionalOnMissingBean(RuleEngineClient.class)
    public RuleEngineClient ruleEngineClient(RuleEngine ruleEngine, RuleRepository ruleRepository) {
        return new RuleEngineClientImpl(ruleEngine, ruleRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoCustomConversions mongoCustomConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new SpecificMongoConverters.CategoryToDBConverter());
        converters.add(new SpecificMongoConverters.SubCategoryToDBConverter());
        converters.add(new SpecificMongoConverters.DBToCategoryConverter());
        converters.add(new SpecificMongoConverters.DBToSubCategoryConverter());
        return new MongoCustomConversions(converters);
    }
}
