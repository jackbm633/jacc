package ast;

class BinOp extends Expression {
  public String operator;
  public Expression lhs;
  public Expression rhs;

  public BinOp(String operator, Expression lhs, Expression rhs) {
    this.operator = operator;
    this.lhs = lhs;
    this.rhs = rhs;
  }

  @Override
  public String toString() {
    return "ast.BinOp{"
            + "operator='" + operator + '\''
            + ", lhs=" + lhs
            + ", rhs=" + rhs
            + '}';
  }
}
