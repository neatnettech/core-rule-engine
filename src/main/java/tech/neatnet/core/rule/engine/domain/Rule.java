package tech.neatnet.core.rule.engine.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rules")
public class Rule implements Serializable {
    @Serial
    @Transient
    private static final long serialVersionUID = 1L;
    @Id
    private String id;
    private Instant dateCreated;
    private Instant dateModified;
    private String modifiedBy;
    private int version;
    private boolean active;
    private String name;
    private String description;
    // TODO: fix fucking converters, or force client to implement
    private String ruleCategory;
    private RuleType ruleType;
    private List<Condition> conditions;
    private Map<String, Object> results;
}
