package BQN.tokenizer.types;

import BQN.tokenizer.Token;

import java.util.List;

public class StrandTok extends TokArr {
  
  public StrandTok(String line, int spos, int epos, List<Token> tokens) {
    super(line, spos, epos, tokens);
  }
  
  public String toRepr() {
    StringBuilder s = new StringBuilder();
    boolean tail = false;
    for (Token v : tokens) {
      if (tail) s.append("‿");
      s.append(v.toRepr());
      tail = true;
    }
    return s.toString();
  }
}