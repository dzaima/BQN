package APL.types.functions.builtins.fns;

import APL.types.*;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.dops.AtBuiltin;

public class ReplicateBuiltin extends Builtin {
  @Override public String repr() {
    return "⌿";
  }
  
  
  
  
  
  public Value underW(Value o, Value w, Value x) {
    Value v = o instanceof Fun? ((Fun) o).call(call(w, x)) : o;
    return AtBuiltin.at(v, new Fun() { // lazy version
      public String repr() { return "{⌿.𝕨}"; }
      public Value call(Value x) { return w; }
    }, x, this);
  }
}