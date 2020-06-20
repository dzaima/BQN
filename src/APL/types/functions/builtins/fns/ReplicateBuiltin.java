package APL.types.functions.builtins.fns;

import APL.types.*;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.dops.AtBuiltin;
import APL.types.functions.builtins.fns2.SlashBuiltin;

public class ReplicateBuiltin extends Builtin {
  @Override public String repr() {
    return "⌿";
  }
  
  
  
  public Value call(Value a, Value w) {
    return SlashBuiltin.replicate(a, w, this);
  }
  
  
  public Value underW(Obj o, Value a, Value w) {
    Value v = o instanceof Fun? ((Fun) o).call(call(a, w)) : (Value) o;
    return AtBuiltin.at(v, new Fun() { // lazy version
      public String repr() { return "{⌿.⍺}"; }
      public Value call(Value w) { return a; }
    }, w, this);
  }
}