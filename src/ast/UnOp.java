package ast;

class UnOp extends Expression {
  public String operator;
  public Expression operand;

  public UnOp(String operator, Expression operand) {
    this.operator = operator;
    this.operand = operand;
  }

  @Override
  public String toString() {
    return "ast.UnOp{" +
            "operator='" + operator + '\'' +
            ", operand=" + operand +
            '}';
  }
}
