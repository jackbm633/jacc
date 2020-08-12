package ast;

class Exp extends Statement {
  public Expression exp;

  public Exp(Expression exp) {
    this.exp = exp;
  }

  @Override
  public String toString() {
    return "ast.Exp{"
            + "exp=" + exp
            + '}';
  }
}
