package tech.neatnet.core.rule.engine.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rule {

  private String condition;
  private String action;
  private List<String> inValues;

}
