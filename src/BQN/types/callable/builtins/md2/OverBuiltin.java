package BQN.types.callable.builtins.md2;

import BQN.tools.FmtInfo;
import BQN.types.Value;
import BQN.types.callable.Md2Derv;
import BQN.types.callable.builtins.Md2Builtin;

public class OverBuiltin extends Md2Builtin {
  public String ln(FmtInfo f) { return "○"; }
  
  public Value call(Value f, Value g, Value x, Md2Derv derv) {
    return f.call(g.call(x));
  }
  public Value call(Value f, Value g, Value w, Value x, Md2Derv derv) {
    return f.call(g.call(w), g.call(x));
  }
  
  public Value callInvX(Value f, Value g, Value w, Value x) {
    return g.callInv(f.callInvX(g.call(w), x));
  }
  public Value callInvW(Value f, Value g, Value w, Value x) {
    return g.callInv(f.callInvW(w, g.call(x)));
  }
  
}