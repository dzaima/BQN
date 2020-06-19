package APL.tokenizer.types;

import APL.Main;
import APL.tokenizer.Token;
import APL.types.arrs.ChrArr;

public class StrTok extends Token {
  public final ChrArr val;
  
  public StrTok(String line, int spos, int epos, String str) {
    super(line, spos, epos);
    val = Main.toAPL(str);
    val.token = this;
    type = 'a';
  }
  
  @Override public String toRepr() {
    return source();
  }
}