package APL.types.functions.builtins.dops;

import APL.types.Value;
import APL.types.functions.DerivedDop;
import APL.types.functions.builtins.DopBuiltin;

public class AfterBuiltin extends DopBuiltin {
  public String repr() {
    return "⟜";
  }
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    return call(f, g, x, x, derv);
  }
  
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    return f.call(w, g.call(x));
  }
  
  public Value callInv(Value f, Value g, Value x) {
    return f.callInvW(x, g.constant(this));
  }
  
  public Value under(Value f, Value g, Value o, Value x, DerivedDop derv) {
    return f.underA(o, x, g.constant(this));
  }
}