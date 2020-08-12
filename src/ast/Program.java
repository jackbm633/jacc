package ast;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Program extends Ast {
  public Function func;

  public Program(Function func) {
    this.func = func;
  }

  @Override
  public String toString() {
    String funcLines =
            Arrays.stream(func.toString().split("\\n"))
                    .map(line -> "\t" + line + "\n")
                    .collect(Collectors.joining());
    return "ast.Program{\n" +
            funcLines +
            '}';
  }
}
