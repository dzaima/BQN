package APL.types.callable.builtins.dops;

import APL.types.Value;
import APL.types.callable.DerivedDop;
import APL.types.callable.builtins.Md2Builtin;

public class OverBuiltin extends Md2Builtin {
  @Override public String repr() {
    return "○";
  }
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    return f.call(g.call(x));
  }
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    return f.call(g.call(w), g.call(x));
  }
  
  public Value callInvX(Value f, Value g, Value w, Value x) {
    return g.callInv(f.callInvX(g.call(w), x));
  }
  public Value callInvW(Value f, Value g, Value w, Value x) {
    return g.callInv(f.callInvW(w, g.call(x)));
  }
  
}