package ast;

class Return extends Statement {
  public Expression exp;

  public Return(Expression exp) {
    this.exp = exp;
  }

  @Override
  public String toString() {
    return "ast.Return{" +
            "exp=" + exp +
            '}';
  }
}
