package APL.types.functions.builtins.fns;

import APL.types.*;
import APL.types.functions.Builtin;

public class OldTallyBuiltin extends Builtin {
  @Override public String repr() {
    return "≢";
  }
  
  
  public Value call(Value x) {
    if (x.rank==0) return Num.ONE;
    return Num.of(x.shape[0]);
  }
  public Value call(Value w, Value x) {
    return w.equals(x)? Num.ZERO : Num.ONE;
  }
}