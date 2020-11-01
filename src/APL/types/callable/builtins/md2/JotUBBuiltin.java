package APL.types.callable.builtins.md2;

import APL.tools.FmtInfo;
import APL.types.Value;
import APL.types.callable.Md2Derv;
import APL.types.callable.builtins.Md2Builtin;

public class JotUBBuiltin extends Md2Builtin {
  public String ln(FmtInfo f) { return "⍛"; }
  
  public Value call(Value f, Value g, Value w, Value x, Md2Derv derv) {
    return g.call(f.call(w), x);
  }
  
  public Value callInvX(Value f, Value g, Value w, Value x) {
    return g.callInvX(f.call(w), x);
  }
  
  public Value callInvW(Value f, Value g, Value w, Value x) {
    return f.callInv(g.callInvW(w, x));
  }
}