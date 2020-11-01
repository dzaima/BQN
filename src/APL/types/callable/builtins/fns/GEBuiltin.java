package APL.types.callable.builtins.fns;

import APL.tools.FmtInfo;
import APL.types.Value;
import APL.types.callable.builtins.FnBuiltin;


public class GEBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "≥"; }
  
  public Value call(Value w, Value x) {
    return LEBuiltin.DF.call(x, w);
  }
}