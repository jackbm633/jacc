package ast;

class Var extends Expression {
  public String name;

  public Var(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "ast.Var{" +
            "name='" + name + '\'' +
            '}';
  }
}
