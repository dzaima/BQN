package BQN.tokenizer.types;

import BQN.tokenizer.Token;
import BQN.types.Nothing;

public class NothingTok extends Token {
  public final Nothing val;
  
  public NothingTok(String raw, int spos, int epos) {
    super(raw, spos, epos);
    val = new Nothing();
    type = 'A';
  }
  
  public String toRepr() {
    return "·";
  }
}