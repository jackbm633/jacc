package ast;

import java.util.ArrayList;

class Function extends Ast {
  public String name;
  public ArrayList<Statement> body;

  public Function(String name, ArrayList<Statement> body) {
    this.name = name;
    this.body = body;
  }

  @Override
  public String toString() {
    StringBuilder bodyLines = new StringBuilder();
    for (Statement s : body) {
      bodyLines.append("\t").append(s).append("\n");
    }
    return "ast.Function " + name + " {\n"
            + bodyLines
            + '}';
  }
}
