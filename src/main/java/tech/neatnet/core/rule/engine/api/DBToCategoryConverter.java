package tech.neatnet.core.rule.engine.api;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import tech.neatnet.core.rule.engine.domain.BaseRuleCategory;

@ReadingConverter
class DBToCategoryConverter implements Converter<String, BaseRuleCategory> {

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

    ;


    @Override
    public <U> Converter<String, U> andThen(Converter<? super BaseRuleCategory, ? extends U> after) {
        return Converter.super.andThen(after);
    }
}