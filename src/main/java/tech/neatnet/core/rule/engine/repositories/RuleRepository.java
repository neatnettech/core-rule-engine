package tech.neatnet.core.rule.engine.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RuleRepository extends MongoRepository<Rule, String> {

    List<Rule> findAllByCategory(Rule.RuleCategory category);

}
