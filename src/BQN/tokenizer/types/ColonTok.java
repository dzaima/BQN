package BQN.tokenizer.types;

import BQN.tokenizer.Token;

public class ColonTok extends Token {
  public ColonTok(String line, int spos, int epos) {
    super(line, spos, epos);
    type = ':';
    flags = 0;
  }
  
  @Override public String toRepr() {
    return ":";
  }
}