package tech.neatnet.core.rule.engine.api;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import tech.neatnet.core.rule.engine.domain.BaseRuleCategory;

@Slf4j
@Configuration
public class RuleEngineMongoConfiguration {

  @ReadingConverter
  public class DBToCategoryConverter implements Converter<String, BaseRuleCategory> {

    @Override
    public BaseRuleCategory convert(String source) {
      try {
        String[] split = source.split(":");
        Class<?> clazz = Class.forName(split[0]);
        if (BaseRuleCategory.class.isAssignableFrom(clazz)) {
          return (BaseRuleCategory) clazz.getMethod("valueOf", String.class).invoke(null, split[1]);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      return null;
    }

    @Override
    public <U> Converter<String, U> andThen(
        Converter<? super BaseRuleCategory, ? extends U> after) {
      return Converter.super.andThen(after);
    }
  }

  @WritingConverter
  public static class CategoryToDBConverter implements Converter<BaseRuleCategory, String> {

    @Override
    public String convert(BaseRuleCategory source) {
      return source.getClass().getName() + ":" + source.getName();
    }

    @Override
    public <U> Converter<BaseRuleCategory, U> andThen(
        Converter<? super String, ? extends U> after) {
      return Converter.super.andThen(after);
    }
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
