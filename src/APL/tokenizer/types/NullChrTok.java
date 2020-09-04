package APL.tokenizer.types;

import APL.types.Char;

public class NullChrTok extends ConstTok {
  
  public NullChrTok(String raw, int spos, int epos) {
    super(raw, spos, epos, Char.of('\0'));
    type = 'A';
  }
  
  public String toRepr() {
    return "@";
  }
}