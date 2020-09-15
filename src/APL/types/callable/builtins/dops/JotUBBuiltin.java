package APL.types.callable.builtins.dops;

import APL.types.Value;
import APL.types.callable.DerivedDop;
import APL.types.callable.builtins.Md2Builtin;

public class JotUBBuiltin extends Md2Builtin {
  @Override public String repr() {
    return "⍛";
  }
  
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    return g.call(f.call(w), x);
  }
  
  public Value callInvX(Value f, Value g, Value w, Value x) {
    return g.callInvX(f.call(w), x);
  }
  
  public Value callInvW(Value f, Value g, Value w, Value x) {
    return f.callInv(g.callInvW(w, x));
  }
}