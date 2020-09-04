package APL.types.functions.builtins.fns2;

import APL.types.*;
import APL.types.functions.Builtin;

public class AssertBuiltin extends Builtin {
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
