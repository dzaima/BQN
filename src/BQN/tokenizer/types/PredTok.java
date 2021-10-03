package BQN.tokenizer.types;

import BQN.tokenizer.Token;

public class PredTok extends Token {
  public PredTok(String line, int spos, int epos) {
    super(line, spos, epos);
    type = '?';
    flags = 0;
  }
  
  @Override public String toRepr() {
    return "?";
  }
}