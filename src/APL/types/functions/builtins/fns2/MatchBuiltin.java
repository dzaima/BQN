package APL.types.functions.builtins.fns2;

import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;

public class MatchBuiltin extends Builtin {
  @Override public String repr() {
    return "≡";
  }
  
  
  public static int lazy(Value w) {
    int depth = 0;
    while (!(w instanceof Primitive)) {
      w = w.first();
      depth++;
    }
    return depth;
  }
  public static int full(Value w) {
    if (w instanceof Primitive) return 0;
    if (w instanceof DoubleArr || w instanceof ChrArr || w instanceof BitArr) return 1;
    int depth = 0;
    for (Value v : w) {
      depth = Math.max(depth, full(v));
    }
    return depth + 1;
  }
  
  public Value call(Value x) {
    return Num.of(full(x));
  }
  public Value call(Value w, Value x) {
    return w.equals(x)? Num.ONE : Num.ZERO;
  }
}
