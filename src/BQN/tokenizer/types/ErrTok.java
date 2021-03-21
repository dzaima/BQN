package BQN.tokenizer.types;

import BQN.tokenizer.Token;

public class ErrTok extends Token {
  public ErrTok(String raw, int spos, int epos) {
    super(raw, Math.max(0,spos), Math.min(epos,raw.length()-1));
  }
  public ErrTok(String raw, int onepos) {
    this(raw, onepos, onepos+1);
  }
  
  @Override public String toRepr() {
    return null;
  }
}