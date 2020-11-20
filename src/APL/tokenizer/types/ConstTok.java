package APL.tokenizer.types;

import APL.tokenizer.Token;
import APL.types.Value;

public abstract class ConstTok extends Token {
  public final Value val;
  
  public ConstTok(String raw, int spos, int epos, Value val) {
    super(raw, spos, epos);
    this.val = val;
    type = 'a';
  }
}