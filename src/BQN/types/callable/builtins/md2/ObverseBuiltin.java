package BQN.types.callable.builtins.md2;

import BQN.tools.FmtInfo;
import BQN.types.Value;
import BQN.types.callable.Md2Derv;
import BQN.types.callable.builtins.Md2Builtin;

public class ObverseBuiltin extends Md2Builtin {
  public String ln(FmtInfo f) { return "⍫"; }
  
  public Value call(Value f, Value g, Value x, Md2Derv derv) {
    return f.call(x);
  }
  public Value call(Value f, Value g, Value w, Value x, Md2Derv derv) {
    return f.call(w, x);
  }
  
  public Value callInv(Value f, Value g, Value x) {
    return g.call(x);
  }
  public Value callInvX(Value f, Value g, Value w, Value x) {
    return g.call(w, x);
  }
  
  public Value callInvW(Value f, Value g, Value w, Value x) { // fall-back to 𝔽
    return f.callInvW(w, x);
  }
}