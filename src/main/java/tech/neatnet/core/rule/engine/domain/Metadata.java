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
public class Metadata {

    private Map<String, Object> inputVariables;
    private long startTimeNanos;
    private long endTimeNanos;
    private boolean failed;
}
