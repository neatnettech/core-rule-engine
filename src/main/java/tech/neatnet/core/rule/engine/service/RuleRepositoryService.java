package tech.neatnet.core.rule.engine.service;

import org.springframework.stereotype.Service;
import tech.neatnet.rule.engine.db.repositories.RuleRepository;
import tech.neatnet.rule.engine.domain.Rule;

import java.util.List;

@Service
public class RuleRepositoryService {

    private final RuleRepository ruleRepository;

    public RuleRepositoryService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    public List<Rule> findAllByCategory(Rule.RuleCategory category) {
        return ruleRepository.findAllByCategory(category);
    }

}
