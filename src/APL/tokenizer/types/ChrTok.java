package APL.tokenizer.types;

import APL.errors.SyntaxError;
import APL.types.Char;

public class ChrTok extends ConstTok {
  
  public ChrTok(String line, int spos, int epos, String str) {
    super(line, spos, epos, Char.of(str.charAt(0)));
    if (str.length() != 1) throw new SyntaxError("single-quote chars must be length 1", this);
  }
  
  @Override public String toRepr() {
    return source();
  }
}