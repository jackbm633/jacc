package ast;

class Constant extends Expression {
  public int constant;

  public Constant(int c) {
    this.constant = c;
  }

  @Override
  public String toString() {
    return "ast.Constant{"
            + "c=" + constant
            + '}';
  }
}
