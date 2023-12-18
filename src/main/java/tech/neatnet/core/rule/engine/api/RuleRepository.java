package tech.neatnet.core.rule.engine.api;

import org.springframework.data.mongodb.repository.MongoRepository;
import tech.neatnet.core.rule.engine.domain.Rule;

import java.util.UUID;

interface RuleRepository extends MongoRepository<Rule, UUID> {

}
