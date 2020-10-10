package APL.tokenizer.types;

import APL.tokenizer.Token;

import java.util.List;

abstract public class TokArr extends Token {
  public final List<Token> tokens;
  
  TokArr(String line, int spos, int epos, List<Token> tokens) {
    super(line, spos, epos);
    this.tokens = tokens;
  }
  
  public String toTree(String p) {
    StringBuilder r = new StringBuilder();
    r.append(p).append(this.getClass().getSimpleName());
    r.append(' ').append(spos).append('-').append(epos).append(' ').append(type).append(' ').append(flags);
    r.append('\n');
    p+= "  ";
    for (Token t : tokens) r.append(t.toTree(p));
    return r.toString();
  }
}