package APL.types.callable.builtins.md2;

import APL.tools.FmtInfo;
import APL.types.Value;
import APL.types.callable.Md2Derv;
import APL.types.callable.builtins.Md2Builtin;

public class AtopBuiltin extends Md2Builtin {
  public String ln(FmtInfo f) { return "∘"; }
  
  public Value call(Value f, Value g, Value x, Md2Derv derv) {
    return f.call(g.call(x));
  }
  public Value call(Value f, Value g, Value w, Value x, Md2Derv derv) {
    return f.call(g.call(w, x));
  }
  
  
  public Value callInv(Value f, Value g, Value x) {
    return f.callInv(g.callInv(x));
  }
  
  public Value callInvX(Value f, Value g, Value w, Value x) {
    return g.callInvX(w, f.callInv(x));
  }
  public Value callInvW(Value f, Value g, Value w, Value x) {
    return g.callInvW(f.callInv(w), x);
  }
  // TODO under
}