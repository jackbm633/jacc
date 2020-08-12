package ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Amd64AssemblyGenerator {
  private final Map<String, Integer> stackOffsets = new HashMap<>();
  private int jumpNum = 0;
  private int currentOffset = 0;

  private String generateJumpLabel() {
    jumpNum++;
    return "..L1." + jumpNum;
  }

  public String generateAssembly(Program ast) {
    return generateFuncString(ast.func);
  }

  /**
   * Generates a string for a specified Function node as assembly language.
   *
   * @param func The Function node to generate assembly for.
   * @return Assembly string for the function.
   */
  public String generateFuncString(Function func) {
    System.out.println(func.body);
    return String.format("\t.globl _%s\n_%s:\n", func.name, func.name)
            + generateBodyString(func.body);
  }


  private String generateBodyString(ArrayList<Statement> s) {
    // Handles case where a return statement is missing, even if there should really be one.
    if (s.size() == 0 || !(s.get(s.size() - 1) instanceof Return)) {
      s.add(new Return(new Constant(0)));
    }
    System.out.println(s.size());
    StringBuilder bodyString = new StringBuilder();
    bodyString.append("\tpush\t%rbp\n");
    bodyString.append("\tmovq\t%rsp, %rbp\n");
    for (Statement st : s) {
      if (st instanceof Return) {
        Return r = (Return) st;
        bodyString.append(generateExpString(r.exp)).append("\n");
        bodyString.append("\tmovq\t%rbp, %rsp\n");
        bodyString.append("\tpopq\t%rbp\n");
        bodyString.append("\tret");
      } else if (st instanceof Exp) {
        Exp e = (Exp) st;
        bodyString.append(generateExpString(e.exp));
      } else if (st instanceof Declare) {
        String declareString = "";
        Declare d = (Declare) st;
        if (stackOffsets.containsKey(d.name)) {
          System.out.println("Error: Variable " + d.name + "declared twice!");
          System.exit(0);
        }
        // Generates expression string and moves it to %rax.
        declareString += "\tsubq\t$4, %rsp\n";
        if (d.init != null) {
          declareString += generateExpString(d.init);
          declareString += "\tmovl\t%eax," + currentOffset + "(%rbp)\n";
        } else {
          declareString += "\tmovl\t$0," + currentOffset + "(%rbp)\n";
        }

        stackOffsets.put(d.name, currentOffset);
        currentOffset -= 4;
        bodyString.append(declareString);
      }
    }


    return bodyString.toString();
  }

  private String generateExpString(Expression s) {
    if (s instanceof Constant) {
      Constant exp = (Constant) s;
      return String.format("\tmovl\t$%d, %%eax\n", exp.constant);
    } else if (s instanceof Var) {

      Var v = (Var) s;
      if (!stackOffsets.containsKey(v.name)) {
        System.out.println("Error: Variable " + v.name + " not declared!");
        System.exit(0);
      }
      return String.format("\tmovl\t%d(%%rbp), %%eax",
              stackOffsets.get(v.name));
    } else if (s instanceof UnOp) {
      UnOp u = (UnOp) s;
      switch (u.operator) {
        case "NEGATE":
          return String.format("%s\n\tnegl\t%%eax", generateExpString(u.operand));
        case "BITWISE_NOT":
          return String.format("%s\n\tnotl\t%%eax", generateExpString(u.operand));
        case "LOGICAL_NOT":
          return String.format("%s\n\ttestl\t%%eax, %%eax\n" // Checks to see operand zero.
                  + "\tsete\t%%al\n" // If zero, set %al.
                  + "\tmovzbl\t%%al, %%eax", generateExpString(u.operand));
        default:
          return null;
      }
    } else if (s instanceof BinOp) {
      BinOp b = (BinOp) s;
      switch (b.operator) {
        case "ADDITION":
          return String.format("%s\n"
                 + "\tpush\t%%rax\n"
                 + "%s\n"
                 + "\tpop\t%%rbx\n"
                 + "\taddl\t%%ebx, %%eax", generateExpString(b.lhs), generateExpString(b.rhs));
        case "NEGATE":
          // Pushes RHS onto stack,
          return String.format("%s\n"
                          + "\tpush\t%%rax\n"
                          + "%s\n"
                          + "\tpop\t%%rbx\n\tsubl\t%%ebx, %%eax",
                  generateExpString(b.rhs), generateExpString(b.lhs));
        case "MULTIPLICATION":
          return String.format("%s\n"
                  + "\tpush\t%%rax\n"
                  + "%s\n"
                  + "\tpop\t%%rbx\n"
                  + "\timull\t%%ebx, %%eax", generateExpString(b.lhs), generateExpString(b.rhs));
        case "DIVISION":
          return String.format("%s\n"
                  + "\tpush\t%%rax\n"
                  + "%s\n"
                  + "\tpop\t%%rbx\n"
                  + "\tcltd\n"
                  + "\tidivl\t%%ebx", generateExpString(b.rhs), generateExpString(b.lhs));
        case "EQUAL":
          return String.format("%s\n\tpush\t%%rax\n%s\n\tpop\t%%rbx\n\tcmpl\t"
                          + "%%eax, %%ebx\n\tmovl\t$0, %%eax\n\tsete\t%%al",
                  generateExpString(b.lhs), generateExpString(b.rhs));
        case "NOT_EQUAL":
          return String.format("%s\n\tpush\t%%rax\n%s\n\tpop\t%%rbx\n\tcmpl\t"
                          + "%%eax, %%ebx\n\tmovl\t$0, %%eax\n\tsetne\t%%al",
                  generateExpString(b.lhs), generateExpString(b.rhs));
        case "GREATER_THAN":
          return String.format("%s\n\tpush\t%%rax\n%s\n\tpop\t%%rbx\n\tcmpl\t"
                          + "%%eax, %%ebx\n\tmovl\t$0, %%eax\n\tsetg\t%%al",
                  generateExpString(b.lhs), generateExpString(b.rhs));
        case "GREATER_THAN_OR_EQUAL":
          return String.format("%s\n\tpush\t%%rax\n%s\n\tpop\t%%rbx\n\tcmpl\t"
                          + "%%eax, %%ebx\n\tmovl\t$0, %%eax\n\tsetge\t%%al",
                  generateExpString(b.lhs), generateExpString(b.rhs));
        case "LESS_THAN":
          return String.format("%s\n\tpush\t%%rax\n%s\n\tpop\t%%rbx\n\tcmpl\t"
                          + "%%eax, %%ebx\n\tmovl\t$0, %%eax\n\tsetl\t%%al",
                  generateExpString(b.lhs), generateExpString(b.rhs));
        case "LESS_THAN_OR_EQUAL":
          return String.format("%s\n\tpush\t%%rax\n%s\n\tpop\t%%rbx\n\tcmpl\t"
                          + "%%eax, %%ebx\n\tmovl\t$0, %%eax\n\tsetle\t%%al",
                  generateExpString(b.lhs), generateExpString(b.rhs));
        case "AND":
          String jumpLabel = this.generateJumpLabel();
          String endJumpLabel = this.generateJumpLabel();
          return String.format("%s\n"  // Code for left hand side.
                          + "\tcmpl\t$0, %%eax\n"  // Checks LHS is 0.
                          + "\tje\t%s\n" // If 0, go to jump and return 0.#
                          + "\txorl\t%%eax, %%eax\n" // Clear EAX register
                          + "%s\n" // Code for right hand side.
                          + "\tcmpl\t$0, %%eax\n" // Checks RHS is 0.
                          + "\tsetne\t%%al\n" // If not 0, return 0.
                          + "\tjmp\t%s\n"
                          + "%s:\n"
                          + "\txorl\t%%eax, %%eax\n"
                          + "\tjmp\t%s\n"
                          + "%s:",

                  generateExpString(b.lhs), jumpLabel,
                  generateExpString(b.rhs), endJumpLabel, jumpLabel,
                  endJumpLabel, endJumpLabel);
        case "OR":
          return String.format("%s\n" + // LHS expression goes here.
                          "\tpush\t%%rax\n" + // Push LHS onto stack.
                          "%s\n" + // RHS expression goes here.
                          "\tpop\t%%rbx\n" + // Move LHS onto RBX.
                          "\torl\t%%eax, %%ebx\n" + // OR the two variables -
                          // if zero. it will return zero.
                          "\tmovl\t$0, %%eax\n" + // Zeroes EAX without affecting flags.
                          "\tsetne\t%%al", // If zero flag is not set, return 1.
                  generateExpString(b.lhs), generateExpString(b.rhs));
        case "MODULO":
          return String.format("%s\n" + // RHS expression goes here.
                          "\tpush\t%%rax\n" + // Push RHS result onto stack.
                          "%s\n" + // RHS expression goes here.
                          "\tpop\t%%rbx\n" + // Move RHS result to rbx.
                          "\tcltd\n" + // Sign extend for division.
                          "\tidivl\t%%ebx\n" + // Perform the division.
                          "\tmovl\t%%edx, %%eax", // Move modulo.
                  generateExpString(b.rhs), generateExpString(b.lhs));
        case "BITWISE_AND":
          return String.format("%s\n" + // LHS expression goes here.
                          "\tpush\t%%rax\n" + // Move LHS onto stack.
                          "%s\n" + // RHS expression fgoes here.
                          "\tpop\t%%rbx\n" + // Move LHS to RBX.
                          "\tandl\t%%ebx, %%eax", // Bitwise AND of LHS and RHS.
                  generateExpString(b.lhs), generateExpString(b.rhs));
        case "BITWISE_XOR":
          return String.format("%s\n" + // LHS expression goes here.
                          "\tpush\t%%rax\n" + // Move LHS onto stack.
                          "%s\n" + // RHS expression fgoes here.
                          "\tpop\t%%rbx\n" + // Move LHS to RBX.
                          "\txorl\t%%ebx, %%eax", // Bitwise XOR of LHS and
                  // RHS.
                  generateExpString(b.lhs), generateExpString(b.rhs));
        case "BITWISE_OR":
          return String.format("%s\n" + // LHS expression goes here.
                          "\tpush\t%%rax\n" + // Move LHS onto stack.
                          "%s\n" + // RHS expression fgoes here.
                          "\tpop\t%%rbx\n" + // Move LHS to RBX.
                          "\torl\t%%ebx, %%eax", // Bitwise OR of LHS and RHS.
                  generateExpString(b.lhs), generateExpString(b.rhs));
        case "LEFT_SHIFT":
          return String.format("%s\n" + // Right hand side expression.
                          "\tpush\t%%rax\n" + // Move RHS onto stack.
                          "%s\n" + // LHS expression
                          "\tpop\t%%rbx\n" + // Move RHS to RBX.
                          "\tshll\t%%bl, %%eax", generateExpString(b.rhs),
                  generateExpString(b.lhs));
        case "RIGHT_SHIFT":
          return String.format("%s\n" + // Right hand side expression.
                          "\tpush\t%%rax\n" + // Move RHS onto stack.
                          "%s\n" + // LHS expression
                          "\tpop\t%%rbx\n" + // Move RHS to RBX.
                          "\tsarl\t%%bl, %%eax", generateExpString(b.rhs),
                  generateExpString(b.lhs));
        default:
          return null;

      }
    } else if (s instanceof Assign) {
      Assign a = (Assign) s;
      Var v = a.var;
      String assignString = "";
      assignString += generateExpString(a.exp) + "\n";
      assignString += String.format("\tmovl\t%%eax, %d(%%rbp)\n",
              stackOffsets.get(v.name));
      return assignString;
    }
    return null;
  }

}
