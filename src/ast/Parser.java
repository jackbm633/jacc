package ast;

import java.util.ArrayList;
import lexer.Lexer;
import lexer.Token;


public class Parser {
  Lexer lexer;
  Token currentToken;

  /**
   * Creates a new Parser based on a lexer.
   * @param lexer The lexer to create a parser.
   */
  public Parser(Lexer lexer) {
    this.lexer = lexer;
    // Set current token to the first token from the input.
    this.currentToken = lexer.token();
  }

  /**
   * "Eats" a token - checks if it is valid, else throws an exception.
   * @param tokenType The type of the token to see if it is valid.
   */
  public void eat(String tokenType) {
    System.out.println(this.currentToken.type);
    try {
      if (this.currentToken.type.equals(tokenType)) {
        this.currentToken = this.lexer.token();
      } else {
        this.error();
      }
    } catch (Exception ex) {
      System.out.println("Syntax error = " + this.currentToken.type);
      System.exit(1);
    }
  }

  /**
   * "Eats" a token and returns the content if valid. Else, it exits the program as there is a
   * syntax error.
   * @param tokenType The type of token that is valid currently.
   * @return The content of the valid token.
   */
  public Object eatPop(String tokenType) {
    System.out.println(this.currentToken.type);
    System.out.println("\t" + this.currentToken.val);
    try {  
      if (this.currentToken.type.equals(tokenType)) {
        Object obj = this.currentToken.val;
        this.currentToken = this.lexer.token();
        return obj;
      } else {
        this.error();
        return null;
      }
    } catch (Exception ex) {
      System.out.println(this.currentToken.val);
      System.out.println("Syntax error = " + this.currentToken.type);
      System.exit(1);
      return null;
    }
  }

  /**
   * Parses a program and returns a Program node.
   * @return The Program node for the current program.
   */
  public Program parseProgram() {
    Function f = this.parseFunction();
    System.out.println(f);
    return new Program(f);
  }
  
  Function parseFunction() {
    this.eat("INT_KEYWORD");
    final String funcName = (String) this.eatPop("IDENTIFIER");
    this.eat("OPEN_BRACKET");
    this.eat("CLOSE_BRACKET");
    this.eat("OPEN_BRACE");
    ArrayList<Statement> body = new ArrayList<>();
    while (!this.currentToken.type.equals("CLOSE_BRACE")) {
      body.add(this.parseStatement());
      System.out.println("\t" + this.currentToken.type);
    }
    System.out.println("Body is " + body);
    this.eat("CLOSE_BRACE");
    return new Function(funcName, body);
  }

  Expression parseExpression() {
    return (parseAssignmentExpression());
  }

  Expression parseAssignmentExpression() {
    Expression logicalOrExpression = parseLogicalOrExpression();
    if (this.currentToken.type.equals("ASSIGN")) {
      this.eat("ASSIGN");
      Expression nextLogicalOrExpression = parseLogicalOrExpression();
      if (logicalOrExpression instanceof Var) {
        logicalOrExpression = new Assign((Var) logicalOrExpression,
                nextLogicalOrExpression);
      } else {
        System.out.println("Syntax error when parsing assignment expression");
        System.exit(-1);
      }
    }
    return logicalOrExpression;
  }

  Statement parseStatement() {
    switch (this.currentToken.type) {
      case "RETURN_KEYWORD":
        this.eat("RETURN_KEYWORD");
        final Expression exp = parseExpression();
        System.out.println("EXPR!");
        this.eat("SEMICOLON");
        return new Return(exp);
      case "INT_KEYWORD":
        this.eat("INT_KEYWORD");
        String id = (String) this.eatPop("IDENTIFIER");
        if (this.currentToken.type.equals("ASSIGN")) {
          this.eat("ASSIGN");
          Expression expr = this.parseExpression();
          this.eat("SEMICOLON");
          return new Declare(id, expr);
        } else {
          this.eat("SEMICOLON");
          return new Declare(id);
        }
      default:
        Expression expr = parseExpression();
        this.eat("SEMICOLON");
        return new Exp(expr);
    }
  }

  Expression parseLogicalOrExpression() {
    Expression logicalAndExpression = parseLogicalAndExpression();
    while (this.currentToken.type.equals("OR")) {
      this.eat("OR");
      Expression nextLogicalAndExpression = parseLogicalAndExpression();
      logicalAndExpression = new BinOp("OR", logicalAndExpression,
              nextLogicalAndExpression);
    }
    return logicalAndExpression;
  }

  Expression parseLogicalAndExpression() {
    Expression bitwiseOrExpression = parseBitwiseOrExpression();
    while (this.currentToken.type.equals("AND")) {
      this.eat("AND");
      Expression nextBitwiseOrExpression = parseBitwiseOrExpression();
      bitwiseOrExpression = new BinOp("AND", bitwiseOrExpression,
              nextBitwiseOrExpression);
    }
    return bitwiseOrExpression;
  }

  Expression parseBitwiseAndExpression() {
    Expression equalityExpression = parseEqualityExpression();
    while (this.currentToken.type.equals("BITWISE_AND")) {
      this.eat("BITWISE_AND");
      Expression nextEqualityExpression = parseEqualityExpression();
      equalityExpression = new BinOp("BITWISE_AND", equalityExpression,
              nextEqualityExpression);
    }
    return equalityExpression;
  }

  Expression parseBitwiseXorExpression() {
    Expression bitwiseAndExpression = parseBitwiseAndExpression();
    while (this.currentToken.type.equals("BITWISE_XOR")) {
      this.eat("BITWISE_XOR");
      Expression nextBitwiseAndExpression = parseBitwiseAndExpression();
      bitwiseAndExpression = new BinOp("BITWISE_XOR", bitwiseAndExpression,
              nextBitwiseAndExpression);
    }
    return bitwiseAndExpression;
  }

  Expression parseBitwiseOrExpression() {
    Expression bitwiseXorExpression = parseBitwiseXorExpression();
    while (this.currentToken.type.equals("BITWISE_OR")) {
      this.eat("BITWISE_OR");
      Expression nextBitwiseXorExpression = parseBitwiseXorExpression();
      bitwiseXorExpression = new BinOp("BITWISE_OR", bitwiseXorExpression,
              nextBitwiseXorExpression);
    }
    return bitwiseXorExpression;
  }

  Expression parseEqualityExpression() {
    Expression relationalExpression = parseRelationalExpression();
    while (this.currentToken.type.equals("EQUAL") || this.currentToken.type.equals("NOT_EQUAL")) {
      if (this.currentToken.type.equals("EQUAL")) {
        this.eat("EQUAL");
        Expression nextRelationalExpression = parseRelationalExpression();
        relationalExpression = new BinOp("EQUAL", relationalExpression,
                nextRelationalExpression);
      } else {
        this.eat("NOT_EQUAL");
        Expression nextRelationalExpression = parseRelationalExpression();
        relationalExpression = new BinOp("NOT_EQUAL", relationalExpression,
                nextRelationalExpression);
      }
    }
    return relationalExpression;
  }

  Expression parseRelationalExpression() {
    Expression shiftExpression = parseShiftExpression();
    while (this.currentToken.type.equals("GREATER_THAN")
            || this.currentToken.type.equals("GREATER_THAN_OR_EQUAL")
            || this.currentToken.type.equals("LESS_THAN_OR_EQUAL")
            || this.currentToken.type.equals("LESS_THAN")) {
      switch (this.currentToken.type) {
        case "GREATER_THAN": {
          this.eat("GREATER_THAN");
          Expression nextShiftExpression = parseShiftExpression();
          shiftExpression = new BinOp("GREATER_THAN", shiftExpression,
                  nextShiftExpression);
          break;
        }
        case "GREATER_THAN_OR_EQUAL": {
          this.eat("GREATER_THAN_OR_EQUAL");
          Expression nextShiftExpression = parseShiftExpression();
          shiftExpression = new BinOp("GREATER_THAN_OR_EQUAL",
                  shiftExpression, nextShiftExpression);
          break;
        }
        case "LESS_THAN": {
          this.eat("LESS_THAN");
          Expression nextShiftExpression = parseAdditiveExpression();
          shiftExpression = new BinOp("LESS_THAN", shiftExpression,
                  nextShiftExpression);
          break;
        }
        default: {
          this.eat("LESS_THAN_OR_EQUAL");
          Expression nextAdditiveExpression = parseAdditiveExpression();
          shiftExpression = new BinOp("LESS_THAN_OR_EQUAL",
                  shiftExpression, nextAdditiveExpression);
          break;
        }
      }
    }
    return shiftExpression;
  }

  Expression parseShiftExpression() {
    Expression additiveExpression = parseAdditiveExpression();
    while (this.currentToken.type.equals("LEFT_SHIFT")
            || this.currentToken.type.equals("RIGHT_SHIFT")) {
      if (this.currentToken.type.equals("LEFT_SHIFT")) {
        this.eat("LEFT_SHIFT");
        Expression nextAdditiveExpression = parseAdditiveExpression();
        additiveExpression = new BinOp("LEFT_SHIFT", additiveExpression,
                nextAdditiveExpression);
      } else {
        this.eat("RIGHT_SHIFT");
        Expression nextAdditiveExpression = parseAdditiveExpression();
        additiveExpression = new BinOp("RIGHT_SHIFT", additiveExpression,
                nextAdditiveExpression);
      }
    }
    return additiveExpression;
  }

  Expression parseAdditiveExpression() {
    Expression term = parseTerm();
    while (this.currentToken.type.equals("ADDITION") || this.currentToken.type.equals("NEGATE")) {
      if (this.currentToken.type.equals("ADDITION")) {
        this.eat("ADDITION");
        Expression nextTerm = parseTerm();
        term = new BinOp("ADDITION", term, nextTerm);
      } else {
        this.eat("NEGATE");
        Expression nextTerm = parseTerm();
        term = new BinOp("NEGATE", term, nextTerm);
      }
    }
    return term;
  }

  Expression parseTerm() {
    Expression factor = parseFactor();
    while (this.currentToken.type.equals("MULTIPLICATION")
            || this.currentToken.type.equals("DIVISION")
            || this.currentToken.type.equals("MODULO")) {
      switch (this.currentToken.type) {
        case "MULTIPLICATION": {
          this.eat("MULTIPLICATION");
          Expression nextFactor = parseFactor();
          factor = new BinOp("MULTIPLICATION", factor, nextFactor);
          break;
        }
        case "DIVISION": {
          this.eat("DIVISION");
          Expression nextFactor = parseFactor();
          factor = new BinOp("DIVISION", factor, nextFactor);
          break;
        }
        case "MODULO": {
          this.eat("MODULO");
          Expression nextFactor = parseFactor();
          factor = new BinOp("MODULO", factor, nextFactor);
          break;
        }
        default:
          factor = null;
      }
    }
    return factor;
  }

  Expression parseFactor() {
    switch (this.currentToken.type) {
      case "OPEN_BRACKET":
        //<factor> ::= "(" <exp> ")"
        this.eat("OPEN_BRACKET");
        Expression exp = parseExpression();
        if (!this.currentToken.type.equals("CLOSE_BRACKET")) {
          // Ensure parens are balanced
          System.out.println("Syntax error");
          System.exit(1);
        } else {
          this.eat("CLOSE_BRACKET");
        }
        return exp;
      case "BITWISE_NOT":
        this.eat("BITWISE_NOT");
        return new UnOp("BITWISE_NOT", parseFactor());
      case "LOGICAL_NOT":
        this.eat("LOGICAL_NOT");
        return new UnOp("LOGICAL_NOT", parseFactor());
      case "NEGATE":
        this.eat("NEGATE");
        return new UnOp("NEGATE", parseFactor());
      case "INT_LITERAL":
        int val = Integer.parseInt((String) this.eatPop("INT_LITERAL"));
        return new Constant(val);
      case "IDENTIFIER":
        String id = (String) this.eatPop("IDENTIFIER");
        return new Var(id);
      default:
        // Fail
        System.out.println("Syntax error");
        System.exit(1);
    }
    return null;
  }
  
  private void error() throws Exception {
    System.out.println("Syntax error");
    throw new Exception("Syntax error");
  }
  
  
}

