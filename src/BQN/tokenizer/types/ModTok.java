package BQN.tokenizer.types;

import BQN.tokenizer.Token;

public class ModTok extends Token {
  public ModTok(String line, int spos, int epos) {
    super(line, spos, epos);
    type = '↩';
  }
  
  @Override public String toRepr() {
    return "←";
  }
}