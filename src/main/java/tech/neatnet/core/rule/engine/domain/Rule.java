package tech.neatnet.core.rule.engine.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rules")
public class Rule implements Serializable {
  @Transient
  private static final long serialVersionUID = 1L;
  private UUID id;
  private Instant dateCreated;
  private Instant dateModified;
  private String modifiedBy;
  private int version;
  private String name;
  private String description;
  private BaseRuleCategory category;
  private RuleType ruleType;
  private HitPolicy hitPolicy;
  private List<Condition> conditions;
  private Map<String, Object> results;
}
