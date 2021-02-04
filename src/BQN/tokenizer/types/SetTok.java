package BQN.tokenizer.types;

import BQN.tokenizer.Token;

public class SetTok extends Token {
  public SetTok(String line, int spos, int epos) {
    super(line, spos, epos);
    type = '←';
  }
  
  @Override public String toRepr() {
    return "←";
  }
}