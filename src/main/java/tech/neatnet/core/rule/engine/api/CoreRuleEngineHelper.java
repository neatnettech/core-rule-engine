package tech.neatnet.core.rule.engine.api;

import java.util.List;
import java.util.Map;

class CoreRuleEngineHelper {

    private CoreRuleEngineHelper() {
    }

    static Map<String, Object> mergeInputVariables(Map<String, Object> inputVariables, List<Object> inValues) {
        if (inValues != null && !inValues.isEmpty()) {
            inputVariables.put("inValues", inValues);
        }
        return inputVariables;
    }

}