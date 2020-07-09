package APL.tokenizer.types;

import APL.errors.SyntaxError;
import APL.types.Char;

public class ChrTok extends ConstTok {
  
  public ChrTok(String line, int spos, int epos, String str) {
    super(line, spos, epos, new Char(str.charAt(0))); // new char because of token binding
    if (str.length() != 1) throw new SyntaxError("single-quote chars must be length 1", this);
  }
  
  @Override public String toRepr() {
    return source();
  }
}