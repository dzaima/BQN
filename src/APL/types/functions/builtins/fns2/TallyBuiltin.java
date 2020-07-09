package APL.types.functions.builtins.fns2;

import APL.Main;
import APL.types.*;
import APL.types.functions.Builtin;

public class TallyBuiltin extends Builtin {
  
  public String repr() {
    return "≢";
  }
  
  public Value call(Value x) {
    return Main.toAPL(x.shape);
  }
  
  public Value call(Value a, Value w) {
    return a.equals(w)? Num.ZERO : Num.ONE;
  }
}
