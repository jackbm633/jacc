package ast;

class Declare extends Statement {
  public String name;
  public Expression init;

  public Declare(String name) {
    this.name = name;
  }

  public Declare(String name, Expression init) {
    this.name = name;
    this.init = init;
  }

  public String toString() {
    if (init != null) {
      return String.format("ast.Declare(%s = %s)\n", name,
              init.toString());
    }
    return String.format("ast.Declare(%s)\n", name);
  }
}
