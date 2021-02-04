package BQN.tokenizer.types;

import BQN.Comp;
import BQN.tokenizer.Token;
import BQN.types.Callable;

public class OpTok extends Token {
  public final String op;
  public final Callable b;
  
  public OpTok(String line, int spos, int epos, String op) {
    super(line, spos, epos);
    this.op = op;
    this.b = Comp.builtin(op.charAt(0), this);
    if (b != null) b.token = this;
  }
  public OpTok(String line, int spos, int epos, char op) {
    super(line, spos, epos);
    this.op = String.valueOf(op);
    this.b = Comp.builtin(op, this);
    if (b != null) b.token = this;
  }
  
  public String toRepr() {
    return op;
  }
}