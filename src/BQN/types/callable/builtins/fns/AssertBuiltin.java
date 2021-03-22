package BQN.types.callable.builtins.fns;

import BQN.tools.*;
import BQN.types.*;
import BQN.types.callable.builtins.FnBuiltin;

public class AssertBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "!"; }
  
  public Value call(Value x) {
    if (x.eq(Num.ONE)) return x;
    throw new BQN.errors.AssertionError("", this);
  }
  
  public Value call(Value w, Value x) {
    if (x.eq(Num.ONE)) return x;
    String msg;
    try {
      msg = Format.outputFmt(w);
    } catch (Throwable t) {
      msg = w.ln(FmtInfo.def);
    }
    throw new BQN.errors.AssertionError(msg, this);
  }
}