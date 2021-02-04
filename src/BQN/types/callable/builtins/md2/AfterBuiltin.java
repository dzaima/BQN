package BQN.types.callable.builtins.md2;

import BQN.tools.FmtInfo;
import BQN.types.Value;
import BQN.types.callable.Md2Derv;
import BQN.types.callable.builtins.Md2Builtin;

public class AfterBuiltin extends Md2Builtin {
  public String ln(FmtInfo f) { return "⟜"; }
  
  public Value call(Value f, Value g, Value x, Md2Derv derv) {
    return call(f, g, x, x, derv);
  }
  
  public Value call(Value f, Value g, Value w, Value x, Md2Derv derv) {
    return f.call(w, g.call(x));
  }
  
  public Value callInv(Value f, Value g, Value x) {
    return f.callInvW(x, g.constant(this, true));
  }
  public Value callInvX(Value f, Value g, Value a, Value w) {
    return g.callInv(f.callInvX(a, w));
  }
  public Value callInvW(Value f, Value g, Value a, Value w) {
    return f.callInvW(a, g.call(w));
  }
  
  public Value under(Value f, Value g, Value o, Value x, Md2Derv derv) {
    return f.underA(o, x, g.constant(this, true));
  }
}