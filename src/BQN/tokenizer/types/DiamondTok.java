package BQN.tokenizer.types;

import BQN.tokenizer.Token;

public class DiamondTok extends Token {
  public DiamondTok(String raw, int pos) {
    super(raw, pos, pos+1);
  }
  
  @Override public String toRepr() {
    return "⋄";
  }
}