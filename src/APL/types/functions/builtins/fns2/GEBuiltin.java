package APL.types.functions.builtins.fns2;

import APL.types.Value;
import APL.types.functions.Builtin;


public class GEBuiltin extends Builtin {
  public String repr() {
    return "≥";
  }
  
  
  
  public Value call(Value w, Value x) {
    return LEBuiltin.DF.call(x, w);
  }
}