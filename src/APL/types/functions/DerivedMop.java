package APL.types.functions;

import APL.types.*;

public class DerivedMop extends Fun {
  public final Value f;
  public final Mop op;
  DerivedMop(Value f, Mop op) {
    this.f = f;
    this.op = op;
    token = op.token;
  }
  
  public Value call(Value x) {
    return op.call(f, x, this);
  }
  public Value call(Value w, Value x) {
    return op.call(f, w, x, this);
  }
  public Value callInv(Value x) {
    return op.callInv(f, x);
  }
  public Value callInvX(Value w, Value x) {
    return op.callInvX(f, w, x);
  }
  public Value callInvW(Value w, Value x) {
    return op.callInvW(f, w, x);
  }
  
  @Override public String repr() {
    return f.oneliner()+op.repr();
  }
  
  public Value under(Value o, Value x) {
    return op.under(f, o, x, this);
  }
  public Value underW(Value o, Value w, Value x) {
    return op.underW(f, o, w, x, this);
  }
  public Value underA(Value o, Value w, Value x) {
    return op.underA(f, o, w, x, this);
  }
}