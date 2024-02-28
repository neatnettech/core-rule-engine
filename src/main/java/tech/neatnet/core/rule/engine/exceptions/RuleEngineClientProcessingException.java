package tech.neatnet.core.rule.engine.exceptions;

public class RuleEngineClientProcessingException extends Throwable {

    public static final String ERR_MSG_EMPTY_INPUT_VARIABLES = "Input variables cannot be empty";
    public static final String ERR_MSG_RULE_CATEGORY = "Rule category cannot be null";
    public static final String ERR_MSG_RULE_SUBCATEGORY = "Rule sub category cannot be null";


    public RuleEngineClientProcessingException(String errMsgEmptyInputVariables) {

    }
}
