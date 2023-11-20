package tech.neatnet.core.rule.engine.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleMatrix {
  private UUID id;
  private int version;
  private Instant timeModified;
  private List<Rule> rules;
}
