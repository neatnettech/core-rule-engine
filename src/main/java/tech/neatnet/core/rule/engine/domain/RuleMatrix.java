package tech.neatnet.core.rule.engine.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ruleMatrices")
public class RuleMatrix {

  private UUID id;
  private Instant dateCreated;
  private Instant dateModified;
  private String modifiedBy;
  private int version;
  private String name;
  private String description;
  private MatrixCategory category;
  private List<Rule> rules;
  private Map<String, Object> results;
}
