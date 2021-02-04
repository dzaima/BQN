package BQN.tokenizer.types;

import BQN.tokenizer.Token;

import java.util.ArrayList;

public class BasicLines extends TokArr {
  public BasicLines(String line, int spos, int epos, ArrayList<Token> tokens) {
    super(line, spos, epos, tokens);
  }
  
  @Override public String toRepr() {
    StringBuilder s = new StringBuilder();
    boolean tail = false;
    for (Token v : tokens) {
      if (tail) s.append("\n");
      s.append(v.toRepr());
      tail = true;
    }
    return s.toString();
  }
}