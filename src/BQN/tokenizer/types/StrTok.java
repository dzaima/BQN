package BQN.tokenizer.types;

import BQN.types.arrs.ChrArr;

public class StrTok extends ConstTok {
  
  public StrTok(String line, int spos, int epos, String str) {
    super(line, spos, epos, new ChrArr(str));
  }
  
  public String toRepr() {
    return source();
  }
}