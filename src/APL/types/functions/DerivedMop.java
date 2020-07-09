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
  public Value callInv(Value w) {
    return op.callInv(aa, w);
  }
  public Value callInvW(Value a, Value w) {
    return op.callInvW(aa, a, w);
  }
  public Value callInvA(Value a, Value w) {
    return op.callInvA(aa, a, w);
  }
  
  @Override public String repr() {
    return aa.toString()+op.repr();
  }
  
  public Value under(Value o, Value w) {
    return op.under(aa, o, w, this);
  }
  public Value underW(Value o, Value a, Value w) {
    return op.underW(aa, o, a, w, this);
  }
  public Value underA(Value o, Value a, Value w) {
    return op.underA(aa, o, a, w, this);
  }
}