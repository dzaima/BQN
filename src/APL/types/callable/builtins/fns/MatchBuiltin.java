package APL.types.callable.builtins.fns;

import APL.tools.FmtInfo;
import APL.types.*;
import APL.types.callable.builtins.FnBuiltin;

public class MatchBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "≡"; }
  
  public static int lazy(Value x) {
    int depth = 0;
    while (!(x instanceof Primitive)) {
      depth++;
      if (x.ia==0) break;
      x = x.first();
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
    return w.eq(x)? Num.ONE : Num.ZERO;
  }
}