package BQN.tokenizer.types;

import BQN.errors.SyntaxError;
import BQN.types.Char;

public class ChrTok extends ConstTok {
  
  public ChrTok(String line, int spos, int epos, String str) {
    super(line, spos, epos, Char.of(str.charAt(0)));
    if (str.length() != 1) throw new SyntaxError("Characters must be UTF-16", this);
  }
  
  public ChrTok(String line, int spos, int epos, char chr) {
    this(line, spos, epos, String.valueOf(chr));
  }
  
  public String toRepr() {
    return source();
  }
}