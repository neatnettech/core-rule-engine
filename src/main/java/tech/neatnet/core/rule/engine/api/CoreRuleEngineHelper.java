package tech.neatnet.core.rule.engine.api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CoreRuleEngineHelper {

    static Map<String, Object> mergeInputVariables(Map<String, Object> inputVariables, List<Object> inValues) {
        if (inValues != null && !inValues.isEmpty()) {
            inputVariables.put("inValues", inValues);
        }
        return inputVariables;
    }

}