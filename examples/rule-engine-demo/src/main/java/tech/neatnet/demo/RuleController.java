package tech.neatnet.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.neatnet.core.rule.engine.api.RuleEngineClient;
import tech.neatnet.core.rule.engine.domain.*;
import tech.neatnet.core.rule.engine.exceptions.RuleEngineClientProcessingException;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleEngineClient ruleEngineClient;

    /**
     * Create a sample decision table rule.
     * POST /api/rules/decision-table
     */
    @PostMapping("/decision-table")
    public ResponseEntity<Rule> createDecisionTableRule(@RequestBody CreateRuleRequest request)
            throws RuleEngineClientProcessingException {
        Rule rule = Rule.builder()
                .name(request.name())
                .description(request.description())
                .ruleType(RuleType.DECISION_TABLE)
                .baseRuleCategory(Category.valueOf(request.category()))
                .baseRuleSubCategory(Category.valueOf(request.subCategory()))
                .conditions(request.conditions().stream()
                        .map(c -> Condition.builder()
                                .condition(c.expression())
                                .inValues(c.inValues())
                                .build())
                        .toList())
                .results(request.results())
                .active(true)
                .build();

        Rule saved = ruleEngineClient.saveRule(rule);
        log.info("Created decision table rule: {}", saved.getName());
        return ResponseEntity.ok(saved);
    }

    /**
     * Create a sample decision tree rule.
     * POST /api/rules/decision-tree
     */
    @PostMapping("/decision-tree")
    public ResponseEntity<Rule> createDecisionTreeRule(@RequestBody CreateTreeRuleRequest request)
            throws RuleEngineClientProcessingException {
        Condition tree = buildTree(request.rootCondition());

        Rule rule = Rule.builder()
                .name(request.name())
                .description(request.description())
                .ruleType(RuleType.DECISION_TREE)
                .baseRuleCategory(Category.valueOf(request.category()))
                .baseRuleSubCategory(Category.valueOf(request.subCategory()))
                .conditions(List.of(tree))
                .active(true)
                .build();

        Rule saved = ruleEngineClient.saveRule(rule);
        log.info("Created decision tree rule: {}", saved.getName());
        return ResponseEntity.ok(saved);
    }

    /**
     * Evaluate rules against input data.
     * POST /api/rules/evaluate
     */
    @PostMapping("/evaluate")
    public ResponseEntity<EvaluationResponse> evaluateRules(@RequestBody EvaluateRequest request)
            throws RuleEngineClientProcessingException {
        log.info("Evaluating rules for category={}, subCategory={}, hitPolicy={}",
                request.category(), request.subCategory(), request.hitPolicy());

        List<RuleExecutionResult> results = ruleEngineClient.evaluateRules(
                request.input(),
                Category.valueOf(request.category()),
                Category.valueOf(request.subCategory()),
                HitPolicy.valueOf(request.hitPolicy())
        );

        List<RuleResult> ruleResults = results.stream()
                .map(r -> new RuleResult(
                        r.getRule().getName(),
                        r.isRuleCriteriaMet(),
                        r.getResults()
                ))
                .toList();

        return ResponseEntity.ok(new EvaluationResponse(ruleResults));
    }

    /**
     * Initialize sample rules for testing.
     * POST /api/rules/init-samples
     */
    @PostMapping("/init-samples")
    public ResponseEntity<Map<String, String>> initSampleRules()
            throws RuleEngineClientProcessingException {
        // Sample 1: Discount rule (Decision Table)
        Rule discountRule = Rule.builder()
                .name("Premium Customer Discount")
                .description("Apply 20% discount for premium customers with orders over $100")
                .ruleType(RuleType.DECISION_TABLE)
                .baseRuleCategory(Category.PRICING)
                .baseRuleSubCategory(Category.DEFAULT)
                .conditions(List.of(
                        Condition.builder().condition("customerType == 'premium'").build(),
                        Condition.builder().condition("orderTotal > 100").build()
                ))
                .results(Map.of(
                        "discountPercent", 20,
                        "message", "Premium customer discount applied"
                ))
                .active(true)
                .build();
        ruleEngineClient.saveRule(discountRule);

        // Sample 2: Standard discount (Decision Table)
        Rule standardDiscount = Rule.builder()
                .name("Standard Customer Discount")
                .description("Apply 5% discount for orders over $200")
                .ruleType(RuleType.DECISION_TABLE)
                .baseRuleCategory(Category.PRICING)
                .baseRuleSubCategory(Category.DEFAULT)
                .conditions(List.of(
                        Condition.builder().condition("orderTotal > 200").build()
                ))
                .results(Map.of(
                        "discountPercent", 5,
                        "message", "Bulk order discount applied"
                ))
                .active(true)
                .build();
        ruleEngineClient.saveRule(standardDiscount);

        // Sample 3: Loan approval (Decision Tree)
        Condition loanTree = Condition.builder()
                .condition("age >= 21")
                .trueBranch(Condition.builder()
                        .condition("income >= 50000")
                        .trueBranch(Condition.builder()
                                .condition("creditScore >= 700")
                                .trueBranch(Condition.builder()
                                        .action("'APPROVED'")
                                        .build())
                                .falseBranch(Condition.builder()
                                        .action("'REVIEW_REQUIRED'")
                                        .build())
                                .build())
                        .falseBranch(Condition.builder()
                                .action("'REJECTED_LOW_INCOME'")
                                .build())
                        .build())
                .falseBranch(Condition.builder()
                        .action("'REJECTED_UNDERAGE'")
                        .build())
                .build();

        Rule loanRule = Rule.builder()
                .name("Loan Approval Process")
                .description("Automated loan approval decision tree")
                .ruleType(RuleType.DECISION_TREE)
                .baseRuleCategory(Category.WORKFLOW)
                .baseRuleSubCategory(Category.DEFAULT)
                .conditions(List.of(loanTree))
                .active(true)
                .build();
        ruleEngineClient.saveRule(loanRule);

        // Sample 4: Validation rule
        Rule validationRule = Rule.builder()
                .name("Email Required")
                .description("Validate that email is provided and valid")
                .ruleType(RuleType.DECISION_TABLE)
                .baseRuleCategory(Category.VALIDATION)
                .baseRuleSubCategory(Category.DEFAULT)
                .conditions(List.of(
                        Condition.builder().condition("email != null").build(),
                        Condition.builder().condition("email.contains('@')").build()
                ))
                .results(Map.of("valid", true))
                .active(true)
                .build();
        ruleEngineClient.saveRule(validationRule);

        log.info("Sample rules initialized");
        return ResponseEntity.ok(Map.of("status", "Sample rules created successfully"));
    }

    private Condition buildTree(TreeConditionRequest req) {
        if (req == null) return null;

        return Condition.builder()
                .condition(req.condition())
                .action(req.action())
                .inValues(req.inValues())
                .trueBranch(buildTree(req.trueBranch()))
                .falseBranch(buildTree(req.falseBranch()))
                .build();
    }

    // Request/Response DTOs

    public record CreateRuleRequest(
            String name,
            String description,
            String category,
            String subCategory,
            List<ConditionRequest> conditions,
            Map<String, Object> results
    ) {}

    public record ConditionRequest(
            String expression,
            List<Object> inValues
    ) {}

    public record CreateTreeRuleRequest(
            String name,
            String description,
            String category,
            String subCategory,
            TreeConditionRequest rootCondition
    ) {}

    public record TreeConditionRequest(
            String condition,
            String action,
            List<Object> inValues,
            TreeConditionRequest trueBranch,
            TreeConditionRequest falseBranch
    ) {}

    public record EvaluateRequest(
            Map<String, Object> input,
            String category,
            String subCategory,
            String hitPolicy
    ) {}

    public record EvaluationResponse(
            List<RuleResult> results
    ) {}

    public record RuleResult(
            String ruleName,
            boolean matched,
            Map<String, Object> output
    ) {}
}
