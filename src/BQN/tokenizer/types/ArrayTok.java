package BQN.tokenizer.types;

import BQN.tokenizer.Token;

import java.util.List;

public class ArrayTok extends TokArr {
  
  public ArrayTok(String line, int spos, int epos, List<Token> tokens) {
    super(line, spos, epos, tokens);
  }
  
  @Override public String toRepr() {
    StringBuilder s = new StringBuilder("⟨");
    boolean tail = false;
    for (Token v : tokens) {
      if (tail) s.append(" ⋄ ");
      s.append(v.toRepr());
      tail = true;
    }
    s.append("⟩");
    return s.toString();
  }
}