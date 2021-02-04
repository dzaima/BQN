package BQN.types.callable.builtins.md2;

import BQN.tools.FmtInfo;
import BQN.types.Value;
import BQN.types.callable.Md2Derv;
import BQN.types.callable.builtins.Md2Builtin;

public class BeforeBuiltin extends Md2Builtin {
  public String ln(FmtInfo f) { return "⊸"; }
  
  public Value call(Value f, Value g, Value x, Md2Derv derv) {
    return call(f, g, x, x, derv);
  }
  
  public Value call(Value f, Value g, Value w, Value x, Md2Derv derv) {
    return g.call(f.call(w), x);
  }
  
  public Value callInv(Value f, Value g, Value x) {
    return g.callInvX(f.constant(this, true), x);
  }
  public Value callInvX(Value f, Value g, Value a, Value w) {
    return g.callInvX(f.call(a), w);
  }
  public Value callInvW(Value f, Value g, Value a, Value w) {
    return f.callInv(g.callInvW(a, w));
  }
  
  public Value under(Value f, Value g, Value o, Value x, Md2Derv derv) {
    return g.underW(o, f.constant(this, true), x);
  }
}