package APL.types.functions.builtins.dops;

import APL.types.Value;
import APL.types.functions.*;

public class BeforeBuiltin extends Dop {
  public String repr() {
    return "⊸";
  }
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    return call(f, g, x, x, derv);
  }
  
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    return g.call(f.call(w), x);
  }
  
  public Value callInv(Value f, Value g, Value x) {
    return g.callInvW(f.constant(this), x);
  }
  
  public Value under(Value f, Value g, Value o, Value x, DerivedDop derv) {
    return g.underW(o, f.constant(this), x);
  }
}