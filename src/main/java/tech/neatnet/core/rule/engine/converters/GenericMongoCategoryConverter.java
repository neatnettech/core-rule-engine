package tech.neatnet.core.rule.engine.converters;

import org.springframework.core.convert.converter.Converter;
import tech.neatnet.core.rule.engine.domain.BaseRuleCategory;

public class GenericMongoCategoryConverter {

    public static class CategoryToDBConverter implements Converter<BaseRuleCategory, String> {
        @Override
        public String convert(BaseRuleCategory source) {
            return source.getClass().getName() + ":" + source.getName();
        }
    }

    public static class DBToCategoryConverter implements Converter<String, BaseRuleCategory> {
        @Override
        public BaseRuleCategory convert(String source) {
            try {
                String[] parts = source.split(":");
                Class<?> clazz = Class.forName(parts[0]);
                if (BaseRuleCategory.class.isAssignableFrom(clazz)) {
                    return (BaseRuleCategory) clazz.getMethod("valueOf", String.class).invoke(null, parts[1]);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert String to Category", e);
            }
            return null;
        }
    }
}