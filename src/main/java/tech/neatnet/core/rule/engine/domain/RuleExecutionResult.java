package tech.neatnet.core.rule.engine.domain;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleExecutionResult {

  private Metadata metadata;
  private Rule rule;
  private Map<String, Object> results;
}



