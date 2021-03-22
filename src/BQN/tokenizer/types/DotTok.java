package BQN.tokenizer.types;

import BQN.tokenizer.Token;

public class DotTok extends Token {
  
  public DotTok(String raw, int spos, int epos) {
    super(raw, spos, epos);
    type = '.';
    flags = 6;
  }
  
  public String toRepr() {
    return ".";
  }
}