package BQN.tokenizer.types;

import BQN.tokenizer.Token;

public class ScopeTok extends Token {
  public ScopeTok(String raw, int spos, int epos) {
    super(raw, spos, epos);
  }
  
  public String toRepr() {
    return "#";
  }
}