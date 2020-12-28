package APL.tokenizer.types;

import APL.types.arrs.ChrArr;

public class StrTok extends ConstTok {
  
  public StrTok(String line, int spos, int epos, String str) {
    super(line, spos, epos, new ChrArr(str));
  }
  
  @Override public String toRepr() {
    return source();
  }
}