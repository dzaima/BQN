package APL.types.functions.builtins.fns2;

import APL.types.*;
import APL.types.arrs.IntArr;
import APL.types.functions.Builtin;

public class TallyBuiltin extends Builtin {
  
  public String repr() {
    return "≢";
  }
  
  public Value call(Value x) {
    return new IntArr(x.shape);
  }
  
  public Value call(Value w, Value x) {
    return w.equals(x)? Num.ZERO : Num.ONE;
  }
}