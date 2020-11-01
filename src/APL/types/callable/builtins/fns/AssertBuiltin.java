package APL.types.callable.builtins.fns;

import APL.tools.*;
import APL.types.*;
import APL.types.callable.builtins.FnBuiltin;

public class AssertBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "!"; }
  
  public Value call(Value x) {
    if (x.eq(Num.ONE)) return x;
    throw new APL.errors.AssertionError("", this);
  }
  
  public Value call(Value w, Value x) {
    if (x.eq(Num.ONE)) return x;
    String msg;
    try {
      msg = Format.outputFmt(w);
    } catch (Throwable t) {
      msg = w.ln(FmtInfo.dbg);
    }
    throw new APL.errors.AssertionError(msg, this);
  }
}
