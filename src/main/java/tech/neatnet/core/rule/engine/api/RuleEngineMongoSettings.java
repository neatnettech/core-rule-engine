package tech.neatnet.core.rule.engine.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RuleEngineMongoSettings {
    private String uri;
}
