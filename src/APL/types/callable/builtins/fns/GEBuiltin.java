package APL.types.callable.builtins.fns;

import APL.types.Value;
import APL.types.callable.builtins.FnBuiltin;


public class GEBuiltin extends FnBuiltin {
  public String repr() {
    return "≥";
  }
  
  
  
  public Value call(Value w, Value x) {
    return LEBuiltin.DF.call(x, w);
  }
}