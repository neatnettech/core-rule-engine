package tech.neatnet.core.rule.engine.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Arrays;

@Configuration
@EnableMongoAuditing
@Slf4j
public class RuleEngineMongoConfiguration {

    @Autowired(required = false)
    private RuleEngineMongoSettings mongoSettings;

    @Bean
    public MongoTemplate mongoTemplate() {
        if (mongoSettings == null) {
            throw new IllegalStateException("Mongo settings not found");
        }
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoSettings.getUri()));
    }

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        log.debug("Creating MongoCustomConversions bean");
        return new MongoCustomConversions(Arrays.asList(
                new CategoryToDBConverter(),
                new DBToCategoryConverter()
        ));
    }
}
