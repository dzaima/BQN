package BQN.tokenizer.types;

import BQN.tokenizer.Token;
import BQN.tools.FmtInfo;
import BQN.types.Value;

public abstract class ConstTok extends Token {
  public final Value val;
  
  public ConstTok(String raw, int spos, int epos, Value val) {
    super(raw, spos, epos);
    this.val = val;
    type = 'a';
  }
  
  
  public String toTree(String p) {
    return p+(getClass().getSimpleName())+" val="+val.ln(FmtInfo.def)+"\n";
  }
}