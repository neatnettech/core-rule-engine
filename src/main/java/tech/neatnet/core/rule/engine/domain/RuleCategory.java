package tech.neatnet.core.rule.engine.domain;

public enum RuleCategory implements BaseRuleCategory {

  DEFAULT;

  @Override
  public String getName() {
    return name();
  }
}