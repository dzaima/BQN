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
  public Value call(Value a, Value w) {
    return on(a, w, this);
  }
  
  public static Value on(Value a, Value w, Callable blame) {
    if (w.rank > 1) throw new RankError("⍳: ⍵ had rank > 1", blame, w);
    if (a.rank > 1) throw new RankError("⍳: ⍺ had rank > 1", blame, a);
    if (w.ia > 20 && a.ia > 20) {
      HashMap<Value, Integer> map = new HashMap<>();
      int ctr = 0;
      for (Value v : a) {
        map.putIfAbsent(v, ctr);
        ctr++;
      }
      double[] res = new double[w.ia];
      ctr = 0;
      double notfound = a.ia;
      for (Value v : w) {
        Integer f = map.get(v);
        res[ctr] = f==null? notfound : f;
        ctr++;
      }
      // w won't be a scalar
      return new DoubleArr(res, w.shape);
    }
    double[] res = new double[w.ia];
    int i = 0;
    for (Value wv : w) {
      int j = 0;
      for (Value av : a) {
        if (av.equals(wv)) break;
        j++;
      }
      res[i++] = j;
    }
    if (w instanceof Primitive) return new Num(res[0]);
    if (w.rank == 0) return new Num(res[0]);
    return new DoubleArr(res, w.shape);
  }
}
