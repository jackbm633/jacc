package ast;

class Assign extends Expression {
  public Var var;
  public Expression exp;

  public Assign(Var var, Expression exp) {
    this.var = var;
    this.exp = exp;
  }


  @Override
  public String toString() {
    return "ast.Assign{"
            + "var=" + var
            + ", exp=" + exp
            + '}';
  }
}
