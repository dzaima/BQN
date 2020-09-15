package APL.types.functions.builtins.fns2;

import APL.types.*;
import APL.types.functions.builtins.FnBuiltin;

public class AssertBuiltin extends FnBuiltin {
  public String repr() {
    return "!";
  }
  
  public Value call(Value x) {
    if (x.eq(Num.ONE)) return x;
    throw new APL.errors.AssertionError("", this);
  }
  
  public Value call(Value w, Value x) {
    if (x.eq(Num.ONE)) return x;
    throw new APL.errors.AssertionError(w.toString(), this);
  }
}
