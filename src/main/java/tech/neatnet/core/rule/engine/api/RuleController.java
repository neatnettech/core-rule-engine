package tech.neatnet.core.rule.engine.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.neatnet.core.rule.engine.domain.Rule;

@RestController
@RequestMapping("/api/v1")
public class RuleController {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @PostMapping("/rules")
    public ResponseEntity<Rule> saveRule(@RequestBody Rule rule) {
      Rule savedRule = ruleService.saveRule(rule);
      return new ResponseEntity<>(savedRule, HttpStatus.CREATED);
    }
}