package APL.tokenizer.types;

import APL.Comp;
import APL.tokenizer.Token;
import APL.types.Callable;

public class OpTok extends Token {
  public final String op;
  public final Callable b;
  
  public OpTok(String line, int spos, int epos, String op) {
    super(line, spos, epos);
    this.op = op;
    this.b = Comp.builtin(this);
    if (b != null) b.token = this;
  }
  
  public String toRepr() {
    return op;
  }
}