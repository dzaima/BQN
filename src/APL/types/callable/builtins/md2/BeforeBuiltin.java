package APL.types.callable.builtins.md2;

import APL.tools.FmtInfo;
import APL.types.Value;
import APL.types.callable.Md2Derv;
import APL.types.callable.builtins.Md2Builtin;

public class BeforeBuiltin extends Md2Builtin {
  public String ln(FmtInfo f) { return "⊸"; }
  
  public Value call(Value f, Value g, Value x, Md2Derv derv) {
    return call(f, g, x, x, derv);
  }
  
  public Value call(Value f, Value g, Value w, Value x, Md2Derv derv) {
    return g.call(f.call(w), x);
  }
  
  public Value callInv(Value f, Value g, Value x) {
    return g.callInvX(f.constant(this), x);
  }
  
  public Value under(Value f, Value g, Value o, Value x, Md2Derv derv) {
    return g.underW(o, f.constant(this), x);
  }
}