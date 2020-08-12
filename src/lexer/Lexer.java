package lexer;

import com.google.code.regexp.Matcher;
import com.google.code.regexp.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {

  Map<String, String> groupType;
  List<Rule> rules;
  Boolean skipWhitespace;
  Pattern regex;
  Pattern reWsSkip;
  private int pos = 0;
  String buf = null;

  public void input(String buf) {
    this.buf = buf;
    this.pos = 0;
  }

  /**
   * Returns the next token in the input string, or null if there are no more tokens to get.
   * @return The next token in the input string.
   */
  public Token token() {
    if (this.pos >= buf.length()) {
      return null;
    } else {
      if (this.skipWhitespace) {
        Matcher m = reWsSkip.matcher(buf);
        m.region(pos, m.regionEnd());
        if (m.find()) {
          this.pos = m.start();
        } else {
          return null;
        }
      }
      Matcher m = regex.matcher(buf);
      m.region(pos, m.regionEnd());
      if (m.find()) {
        List<String> orderedGroups = m.orderedGroups();
        Token tok = null;
        for (int i = 0; i < orderedGroups.size(); i++) {
          if (orderedGroups.get(i) != null) {
            tok = new Token(rules.get(i).ruleName, orderedGroups.get(i), pos);
          }
        }
        pos = m.end();
        return tok;
      }
    }
    return null;
  }


  /**
   * Gets the list of tokens in the input string.
   * @return A list of tokens, starting from the first token.
   */
  public List<Token> getTokens() {
    ArrayList<Token> tokens = new ArrayList<>();
    while (true) {
      Token tok = this.token();
      if (tok == null) {
        break;
      }
      tokens.add(tok);
    }
    return tokens;
  }

  /**
   * Constructs a new Lexer based on a list of rules and whether we want to skip whitespace.
   * @param rules The list of rules that we want the lexer to follow (Regular expressions)
   * @param skipWhitespace Whether we want to skip whitespace or not.
   */
  public Lexer(ArrayList<Rule> rules, boolean skipWhitespace) {
    int idx = 1;
    ArrayList<String> regexParts = new ArrayList<String>();
    this.groupType = new HashMap<String, String>();
    this.rules = rules;
    for (Rule rule : rules) {
      String groupname = "GROUP$idx";
      regexParts.add(String.format("(?<%s>%s)", groupname, rule.regex));
      groupType.put(groupname, rule.ruleName);
      idx += 1;
    }
    regex = Pattern.compile(String.join("|", regexParts));
    this.skipWhitespace = skipWhitespace;
    this.reWsSkip = Pattern.compile("\\S");
  }

}

