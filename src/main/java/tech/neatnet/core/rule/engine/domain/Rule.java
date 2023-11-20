package tech.neatnet.core.rule.engine.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rule {
    private UUID id;
    private int version;
    private RuleCategory category;
    private String condition;
    private String action;
    private List<String> inValues;

    public enum RuleCategory {
        LARGE,
        ANOMALY,
        PASS_THROUGH
    }
}
