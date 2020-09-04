package APL.types.functions.builtins.dops;

import APL.types.Value;
import APL.types.functions.*;

public class OverBuiltin extends Dop {
  @Override public String repr() {
    return "○";
  }
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    return f.call(g.call(x));
  }
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    return f.call(g.call(w), g.call(x));
  }
  
  public Value callInvW(Value f, Value g, Value w, Value x) {
    return g.callInv(f.callInvW(g.call(w), x));
  }
  public Value callInvA(Value f, Value g, Value w, Value x) {
    return g.callInv(f.callInvA(w, g.call(x)));
  }
  
}