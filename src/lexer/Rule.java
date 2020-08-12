package lexer;

public class Rule {
  public String regex;
  public String ruleName;

  public Rule(String regex, String ruleName) {
    this.regex = regex;
    this.ruleName = ruleName;
  }
}
