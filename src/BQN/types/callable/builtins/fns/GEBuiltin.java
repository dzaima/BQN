package BQN.types.callable.builtins.fns;

import BQN.tools.FmtInfo;
import BQN.types.*;
import BQN.types.callable.builtins.FnBuiltin;


public class GEBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "≥"; }
  public Value identity() { return Num.ONE; }
  
  public Value call(Value w, Value x) {
    return LEBuiltin.DF.call(x, w);
  }
}