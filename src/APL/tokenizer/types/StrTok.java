package APL.tokenizer.types;

import APL.Main;

public class StrTok extends ConstTok {
  
  public StrTok(String line, int spos, int epos, String str) {
    super(line, spos, epos, Main.toAPL(str));
  }
  
  @Override public String toRepr() {
    return source();
  }
}