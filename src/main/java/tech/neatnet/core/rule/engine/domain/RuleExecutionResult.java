package tech.neatnet.core.rule.engine.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleExecutionResult {
    private Metadata metadata;
    private Rule rule;
    private Map<String, Object> results; // assuming this could be any type
}



