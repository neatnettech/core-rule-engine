package tech.neatnet.core.rule.engine.api;

import org.springframework.data.mongodb.repository.MongoRepository;
import tech.neatnet.core.rule.engine.domain.Rule;
import tech.neatnet.core.rule.engine.domain.RuleType;

import java.util.List;
import java.util.UUID;

interface RuleRepository extends MongoRepository<Rule, UUID> {

    List<Rule> getRulesByRuleType(RuleType ruleType);

}
