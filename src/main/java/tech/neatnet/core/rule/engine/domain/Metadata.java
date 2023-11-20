package tech.neatnet.core.rule.engine.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Metadata {
  private Map<String, Object> inputVariables;
  private Instant startTime;
  private Instant endTime;
  private boolean failed;
}
