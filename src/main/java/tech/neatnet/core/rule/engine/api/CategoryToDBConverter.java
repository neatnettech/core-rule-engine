package tech.neatnet.core.rule.engine.api;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;
import tech.neatnet.core.rule.engine.domain.BaseRuleCategory;

@Component
@WritingConverter
class CategoryToDBConverter implements Converter<BaseRuleCategory, String> {

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