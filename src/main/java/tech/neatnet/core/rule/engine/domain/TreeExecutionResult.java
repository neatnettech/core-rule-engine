package tech.neatnet.core.rule.engine.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreeExecutionResult {
    private Rule rule;
    private Condition condition;
    private boolean ruleCriteriaMet;
    private Map<String, Object> results;
    private List<Condition> executedNodes;
}
