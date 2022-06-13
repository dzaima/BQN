package BQN.types.callable.builtins.fns;

import BQN.errors.*;
import BQN.tools.FmtInfo;
import BQN.types.*;
import BQN.types.arrs.IntArr;
import BQN.types.callable.builtins.FnBuiltin;
import BQN.types.callable.builtins.md1.CellBuiltin;

import java.util.HashMap;

public class RBoxBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "⊐"; }
  
  public Value call(Value x) {
    HashMap<Value, Integer> map = new HashMap<Value, Integer>();
    if (x.r() == 0) throw new DomainError("⊐: argument cannot be a scalar", this);
    Value[] xv = x.r()==1? x.values() : CellBuiltin.cells(x);
    int[] res = new int[xv.length];
    int am = 0;
    for (int i = 0; i < xv.length; i++) {
      Value c = xv[i];
      Integer prev = map.get(c);
      if (prev == null) {
        prev = am++;
        map.put(c, prev);
      }
      res[i] = prev;
    }
    return new IntArr(res);
  }
  
  public Value call(Value w, Value x) {
    return on(w, x, this);
  }
  
  public static Value on(Value w, Value x, Callable blame) {
    if (x.r() > 1) throw new RankError("⊐: 𝕩 had rank > 1", blame);
    if (w.r() > 1) throw new RankError("⊐: 𝕨 had rank > 1", blame);
    if (x.ia > 20 && w.ia > 20) {
      if (w.quickIntArr() && x.quickIntArr()) {
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        int ctr = 0;
        for (int v : w.asIntArr()) map.putIfAbsent(v, ctr++);
        int[] res = new int[x.ia];
        int[] xi = x.asIntArr();
        for (int i = 0; i < xi.length; i++) {
          Integer f = map.get(xi[i]);
          res[i] = f==null? w.ia : f;
        }
        return new IntArr(res, x.shape);
      } else {
        HashMap<Value, Integer> map = new HashMap<Value, Integer>();
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
    } else {
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
}