package BQN.tokenizer.types;

import BQN.tokenizer.Token;

public class StranderTok extends Token {
  public StranderTok(String raw, int spos, int epos) {
    super(raw, spos, epos);
  }
  
  public String toRepr() {
    return "‿";
  }
}