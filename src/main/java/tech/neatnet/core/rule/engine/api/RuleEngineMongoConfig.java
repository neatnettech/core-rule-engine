package tech.neatnet.core.rule.engine.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
class RuleEngineMongoConfig {

    @Autowired(required = false)
    private RuleEngineMongoSettings mongoSettings;

    @Bean
    public MongoTemplate mongoTemplate() {
        if (mongoSettings == null) {
            throw new IllegalStateException("Mongo settings not found");
        }
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoSettings.getUri()));
    }

}
