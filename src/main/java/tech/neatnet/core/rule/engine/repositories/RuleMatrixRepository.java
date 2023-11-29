package tech.neatnet.core.rule.engine.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import tech.neatnet.core.rule.engine.domain.RuleMatrix;

public interface RuleMatrixRepository extends MongoRepository<RuleMatrix, String> {

}
