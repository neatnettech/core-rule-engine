package tech.neatnet.core.rule.engine.api;

import org.springframework.data.mongodb.repository.MongoRepository;

import tech.neatnet.core.rule.engine.domain.Rule;

import java.util.List;

interface RuleRepository extends MongoRepository<Rule, String> {
    
    List<Rule> findRulesByBaseRuleCategoryAndBaseRuleSubCategory(BaseRuleCategory baseRuleCategory, BaseRuleSubCategory baseRuleSubCategory);
    
    List<Rule> findRulesByActive(boolean active);

}
