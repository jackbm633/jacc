import ast.Amd64AssemblyGenerator;
import ast.Parser;
import ast.Program;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import lexer.Lexer;
import lexer.Rule;
import org.apache.commons.io.FilenameUtils;




class Main {
  static ArrayList<Rule> rules =
          new ArrayList<>(Arrays.asList(new Rule("\\{", "OPEN_BRACE"),
                  new Rule("\\}", "CLOSE_BRACE"),
                  new Rule("\\(", "OPEN_BRACKET"),
                  new Rule("\\)", "CLOSE_BRACKET"),
                  new Rule(";", "SEMICOLON"),
                  new Rule("int\\s", "INT_KEYWORD"),
                  new Rule("return\\b", "RETURN_KEYWORD"),
                  new Rule(";", "SEMICOLON"),
                  new Rule("[a-zA-Z]\\w*", "IDENTIFIER"),
                  new Rule("[0-9]+", "INT_LITERAL"),
                  new Rule("-", "NEGATE"),
                  new Rule("~", "BITWISE_NOT"),
                  new Rule("!=", "NOT_EQUAL"),
                  new Rule("!", "LOGICAL_NOT"),
                  new Rule("\\+", "ADDITION"),
                  new Rule("/", "DIVISION"),
                  new Rule("\\*", "MULTIPLICATION"),
                  new Rule("&&", "AND"),
                  new Rule("\\|\\|", "OR"),
                  new Rule("==", "EQUAL"),

                  new Rule("<=", "LESS_THAN_OR_EQUAL"),
                  new Rule("<", "LESS_THAN"),
                  new Rule(">=", "GREATER_THAN_OR_EQUAL"),

                  new Rule(">", "GREATER_THAN"),

                  new Rule("%", "MODULO"),
                  new Rule("&", "BITWISE_AND"),
                  new Rule("\\^", "BITWISE_XOR"),
                  new Rule("\\|", "BITWISE_OR"),
                  new Rule("<<", "LEFT_SHIFT"),
                  new Rule(">>", "RIGHT_SHIFT"),
                  new Rule("=", "ASSIGN")));


  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("jacc: Must specify input C file.");
      System.exit(1);
    }
    String fileName = args[0];
    Lexer lx = new Lexer(rules, true);
    String content = "";
    try {
      content = new String(Files.readAllBytes(Paths.get(args[0])));
    } catch (IOException ioe) {
      System.err.println("Could not open file " + args[0]);
      ioe.printStackTrace();
      System.exit(0);
    }
    lx.input(content);
    Parser p = new Parser(lx);
    Program prog = p.parseProgram();
    System.out.println(prog.toString());
    Amd64AssemblyGenerator a = new Amd64AssemblyGenerator();
    String assy = (a.generateAssembly(prog));
    System.out.println(assy);
    try {
      PrintWriter out =
              new PrintWriter(FilenameUtils.removeExtension(args[0]) + ".s");
      out.println(assy);
      out.close();
      Process proc = Runtime.getRuntime().exec(String.format("gcc %s -o"
                      + " %s", FilenameUtils.removeExtension(fileName) + ".s",
              FilenameUtils.removeExtension(fileName)));
      BufferedReader reader =
              new BufferedReader(new InputStreamReader(proc.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        System.out.println(line);
      }
    } catch (IOException ioe) {
      System.err.println("Could not write assembly for file " + args[0]);
      ioe.printStackTrace();
      System.exit(0);
    }
  }
}