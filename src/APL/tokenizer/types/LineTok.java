package APL.tokenizer.types;

import APL.tokenizer.Token;

import java.util.List;

public class LineTok extends TokArr { // +TODO make things using this verify end
  
  public LineTok(String raw, int spos, int epos, List<Token> tokens) {
    super(raw, spos, epos, tokens);
  }
  
  public String toString() {
    return toRepr();
  }
  
  @Override public String toRepr() {
    StringBuilder s = new StringBuilder();
    boolean tail = false;
    for (Token v : tokens) {
      if (tail) s.append(" ");
      s.append(v.toRepr());
      tail = true;
    }
    return s.toString();
  }
}