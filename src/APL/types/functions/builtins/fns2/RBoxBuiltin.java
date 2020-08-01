package APL.types.functions.builtins.fns2;

import APL.errors.RankError;
import APL.types.*;
import APL.types.arrs.IntArr;
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
      if (w.quickIntArr() && x.quickIntArr()) {
        HashMap<Integer, Integer> map = new HashMap<>();
        int ctr = 0;
        for (int v : w.asIntArr()) map.putIfAbsent(v, ctr++);
        int[] res = new int[x.ia];
        int[] xi = x.asIntArr();
        for (int i = 0; i < xi.length; i++) {
          Integer f = map.get(xi[i]);
          res[i] = f==null? w.ia : f;
        }
        return new IntArr(res, x.shape);
      }
      HashMap<Value, Integer> map = new HashMap<>();
      int ctr = 0;
      for (Value v : w) map.putIfAbsent(v, ctr++);
      int[] res = new int[x.ia];
      ctr = 0;
      for (Value v : x) {
        Integer f = map.get(v);
        res[ctr++] = f==null? w.ia : f;
      }
      return new IntArr(res, x.shape);
    }
    int[] res = new int[x.ia];
    int i = 0;
    if (w.quickIntArr() && x.quickIntArr()) {
      int[] wi = w.asIntArr();
      for (int cx : x.asIntArr()) {
        int j = 0;
        for (int cw : wi) {
          if (cw == cx) break;
          j++;
        }
        res[i++] = j;
      }
    } else {
      Value[] wv = w.values();
      for (Value cx : x) {
        int j = 0;
        for (Value cw : wv) {
          if (cw.eq(cx)) break;
          j++;
        }
        res[i++] = j;
      }
    }
    return new IntArr(res, x.shape);
  }
}