package APL.types.functions;

import APL.types.*;

public class DerivedMop extends Fun {
  public final Value aa;
  public final Mop op;
  DerivedMop(Value aa, Mop op) {
    this.aa = aa;
    this.op = op;
    token = op.token;
  }
  
  public Value call(Value x) {
    return op.call(aa, x, this);
  }
  public Value call(Value w, Value x) {
    return op.call(aa, w, x, this);
  }
  public Value callInv(Value x) {
    return op.callInv(aa, x);
  }
  public Value callInvW(Value w, Value x) {
    return op.callInvW(aa, w, x);
  }
  public Value callInvA(Value w, Value x) {
    return op.callInvA(aa, w, x);
  }
  
  @Override public String repr() {
    return aa.toString()+op.repr();
  }
  
  public Value under(Value o, Value x) {
    return op.under(aa, o, x, this);
  }
  public Value underW(Value o, Value w, Value x) {
    return op.underW(aa, o, w, x, this);
  }
  public Value underA(Value o, Value w, Value x) {
    return op.underA(aa, o, w, x, this);
  }
}