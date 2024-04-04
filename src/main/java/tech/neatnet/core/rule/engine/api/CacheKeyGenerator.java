package tech.neatnet.core.rule.engine.api;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component("ruleKeyGenerator")
public class CacheKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        BaseRuleCategory baseRuleCategory = (BaseRuleCategory) params[0];
        BaseRuleSubCategory baseRuleSubCategory = (BaseRuleSubCategory) params[1];

        String categoryKey = baseRuleCategory != null && baseRuleCategory.getName() != null ? 
                             baseRuleCategory.getName() : "unknownCategory";
        String subCategoryKey = baseRuleSubCategory != null && baseRuleSubCategory.getName() != null ? 
                                baseRuleSubCategory.getName() : "unknownSubCategory";

        return categoryKey + ":" + subCategoryKey;
    }
}