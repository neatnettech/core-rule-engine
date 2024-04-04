package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.ehcache.config.CacheConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "rule.engine.enabled", havingValue = "true", matchIfMissing = true)
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
    public RuleCache ruleMatrixCache(RuleRepositoryService ruleRepositoryService) {
        log.debug("Creating RuleCache bean");
        return new RuleCacheImpl(ruleRepositoryService);
    }

    @Bean
    @ConditionalOnMissingBean
    public RuleEngine ruleEngine(CoreRuleEngine coreRuleEngine, RuleCache ruleCache) {
        log.debug("Creating RuleEngine bean");
        return new RuleEngine(coreRuleEngine, ruleCache);
    }

    @Bean
    public RuleEngineClientImpl ruleEngineClient(RuleEngine ruleEngine, RuleRepository ruleRepository) {
        log.debug("Creating RuleEngineClient bean");
        return new RuleEngineClientImpl(ruleEngine, ruleRepository);
    }
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new SpecificMongoConverters.CategoryToDBConverter());
        converters.add(new SpecificMongoConverters.SubCategoryToDBConverter());
        converters.add(new SpecificMongoConverters.DBToCategoryConverter());
        converters.add(new SpecificMongoConverters.DBToSubCategoryConverter());
        return new MongoCustomConversions(converters);
    }
}