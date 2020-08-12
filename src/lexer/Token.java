package lexer;


public class Token {
  public String type;
  public Object val;
  public int pos;

  /**
   * Creates a new Token.
   * @param type The type of the token.
   * @param val The value of the token.
   * @param pos The position of the token.
   */
  public Token(String type, Object val, int pos) {
    this.type = type;
    this.val = val;
    this.pos = pos;
  }
}