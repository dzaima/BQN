package APL.types.callable.builtins.dops;

import APL.types.Value;
import APL.types.callable.DerivedDop;
import APL.types.callable.builtins.DopBuiltin;

public class BeforeBuiltin extends DopBuiltin {
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
    return g.callInvX(f.constant(this), x);
  }
  
  public Value under(Value f, Value g, Value o, Value x, DerivedDop derv) {
    return g.underW(o, f.constant(this), x);
  }
}