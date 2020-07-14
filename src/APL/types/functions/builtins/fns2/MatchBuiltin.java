package APL.types.functions.builtins.fns2;

import APL.types.*;
import APL.types.functions.Builtin;

public class MatchBuiltin extends Builtin {
  @Override public String repr() {
    return "≡";
  }
  
  
  public static int lazy(Value x) {
    int depth = 0;
    while (!(x instanceof Primitive)) {
      x = x.first();
      depth++;
    }
    return depth;
  }
  public static int full(Value x) {
    if (x instanceof Primitive) return 0;
    if (x.quickDepth1()) return 1;
    int depth = 0;
    for (Value v : x) {
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