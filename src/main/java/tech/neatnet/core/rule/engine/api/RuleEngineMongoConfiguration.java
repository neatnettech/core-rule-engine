package tech.neatnet.core.rule.engine.api;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import tech.neatnet.core.rule.engine.domain.BaseRuleCategory;

@Slf4j
@Configuration
public class RuleEngineMongoConfiguration extends AbstractMongoClientConfiguration {

  @Autowired(required = false)
  private RuleEngineMongoSettings ruleEngineMongoSettings;

  @Override
  protected String getDatabaseName() {
    return "rule-engine";
  }

  @Override
  public MongoClient mongoClient() {
    return MongoClients.create(ruleEngineMongoSettings.getUri());
  }

  @Override
  public MongoCustomConversions customConversions() {
    List<Converter<?, ?>> converters = new ArrayList<>();
    // Add your converters here
    converters.add(new DBToCategoryConverter());
    converters.add(new CategoryToDBConverter());
    return new MongoCustomConversions(converters);
  }

}
