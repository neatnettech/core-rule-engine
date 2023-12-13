package tech.neatnet.core.rule.engine.api;

import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import tech.neatnet.core.rule.engine.domain.RuleMatrix;

interface RuleMatrixRepository extends MongoRepository<RuleMatrix, UUID> {

}
