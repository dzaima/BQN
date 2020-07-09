package APL.types.functions.builtins.fns2;

import APL.errors.RankError;
import APL.types.*;
import APL.types.arrs.DoubleArr;
import APL.types.functions.Builtin;

import java.util.HashMap;

public class RBoxBuiltin extends Builtin {
  public String repr() {
    return "⊐";
  }
  public Value call(Value w, Value x) {
    return on(w, x, this);
  }
  
  public static Value on(Value w, Value x, Callable blame) {
    if (x.rank > 1) throw new RankError("⊐: 𝕩 had rank > 1", blame, x);
    if (w.rank > 1) throw new RankError("⊐: 𝕨 had rank > 1", blame, w);
    if (x.ia > 20 && w.ia > 20) {
      HashMap<Value, Integer> map = new HashMap<>();
      int ctr = 0;
      for (Value v : w) {
        map.putIfAbsent(v, ctr);
        ctr++;
      }
      double[] res = new double[x.ia];
      ctr = 0;
      double notfound = w.ia;
      for (Value v : x) {
        Integer f = map.get(v);
        res[ctr] = f==null? notfound : f;
        ctr++;
      }
      // w won't be a scalar
      return new DoubleArr(res, x.shape);
    }
    double[] res = new double[x.ia];
    int i = 0;
    for (Value cx : x) {
      int j = 0;
      for (Value cw : w) {
        if (cw.equals(cx)) break;
        j++;
      }
      res[i++] = j;
    }
    return new DoubleArr(res, x.shape);
  }
}