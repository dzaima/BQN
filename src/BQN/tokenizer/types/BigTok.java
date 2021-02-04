package BQN.tokenizer.types;

import BQN.types.BigValue;

public class BigTok extends ConstTok {
  public BigTok(String line, int spos, int epos, BigValue val) {
    super(line, spos, epos, val);
  }
  @Override public String toRepr() {
    return source();
  }
}