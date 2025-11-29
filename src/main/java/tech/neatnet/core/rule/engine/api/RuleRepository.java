package tech.neatnet.core.rule.engine.api;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import tech.neatnet.core.rule.engine.domain.Rule;

import java.util.List;

interface RuleRepository extends MongoRepository<Rule, String> {

    @Query("{ 'baseRuleCategory': ?0, 'baseRuleSubCategory': ?1, 'active': true }")
    List<Rule> findRulesByBaseRuleCategoryAndBaseRuleSubCategory(String baseRuleCategory, String baseRuleSubCategory);

    List<Rule> findRulesByActive(boolean active);
}
