package tech.neatnet.core.rule.engine.api;

import org.springframework.data.mongodb.repository.MongoRepository;
import tech.neatnet.core.rule.engine.domain.Rule;

import java.util.UUID;

interface DecisionTreeRepository extends MongoRepository<Rule, UUID> {
}
