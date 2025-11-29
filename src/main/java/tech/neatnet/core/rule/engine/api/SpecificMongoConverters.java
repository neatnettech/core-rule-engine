package tech.neatnet.core.rule.engine.api;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;


@Component
public class SpecificMongoConverters {
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
    @WritingConverter
    public static class SubCategoryToDBConverter implements Converter<BaseRuleSubCategory, String> {

        @Override
        public String convert(BaseRuleSubCategory source) {
            return source.getClass().getName() + ":" + source.getName();
        }

        @Override
        public <U> Converter<BaseRuleSubCategory, U> andThen(
                Converter<? super String, ? extends U> after) {
            return Converter.super.andThen(after);
        }
    }

    @ReadingConverter
    public static class DBToCategoryConverter implements Converter<String, BaseRuleCategory> {

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

    @ReadingConverter
    public static class DBToSubCategoryConverter implements Converter<String, BaseRuleSubCategory> {

        @Override
        public BaseRuleSubCategory convert(String source) {
            try {
                String[] split = source.split(":");
                Class<?> clazz = Class.forName(split[0]);
                if (BaseRuleSubCategory.class.isAssignableFrom(clazz)) {
                    return (BaseRuleSubCategory) clazz.getMethod("valueOf", String.class).invoke(null, split[1]);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        @Override
        public <U> Converter<String, U> andThen(
                Converter<? super BaseRuleSubCategory, ? extends U> after) {
            return Converter.super.andThen(after);
        }
    }
}
