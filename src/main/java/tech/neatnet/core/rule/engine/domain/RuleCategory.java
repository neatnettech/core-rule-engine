package tech.neatnet.core.rule.engine.domain;

public enum RuleCategory implements BaseRuleCategory {

  DUMMY_SIGNAL;

  @Override
  public String getName() {
    return name();
  }
}