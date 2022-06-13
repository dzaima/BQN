package BQN.types.callable.builtins.fns;

import BQN.errors.RankError;
import BQN.tools.FmtInfo;
import BQN.types.*;
import BQN.types.arrs.BitArr;
import BQN.types.callable.builtins.FnBuiltin;
import BQN.types.callable.builtins.md1.CellBuiltin;

import java.util.HashSet;

public class EpsBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "∊"; }
  
  public Value call(Value x) {
    if (x.r() == 0) throw new RankError("∊: argument cannot be scalar", this);
    Value[] vs;
    BitArr.BA res;
    if (x.r() == 1) {
      res = new BitArr.BA(x.shape,true);
      if (x.quickIntArr()) {
        HashSet<Integer> seen = new HashSet<>();
        for (int c : x.asIntArr()) res.add(seen.add(c));
        return res.finish();
      }
      vs = x.values();
    } else {
      res = new BitArr.BA(Arr.vecsh(x.shape[0]),true);
      vs = CellBuiltin.cells(x);
    }
    HashSet<Value> seen2 = new HashSet<>();
    for (Value c : vs) res.add(seen2.add(c));
    return res.finish();
  }
  
  public Value call(Value w, Value x) {
    if (w.scalar()) { // TODO this _might_ be wrong
      Value w0 = w.first();
      for (Value v : x) {
        if (v.eq(w0)) return Num.ONE;
      }
      return Num.ZERO;
    }
    BitArr.BA res = new BitArr.BA(w.shape,true);
    if (w.ia>20 && x.ia>20) { // TODO these (and in ⊐) shouldn't be random numbers
      HashSet<Value> vs = new HashSet<>();
      for (Value c : x) vs.add(c);
      for (Value c : w) res.add(vs.contains(c));
    } else {
      if (x.quickIntArr() && w.quickIntArr()) {
        int[] xi = x.asIntArr();
        int[] wi = w.asIntArr();
        for (int i = 0; i < w.ia; i++) {
          int cw = wi[i];
          boolean b = false;
          for (int v : xi) {
            if (v == cw) {
              b = true;
              break;
            }
          }
          res.add(b);
        }
      } else {
        Value[] xv = x.values();
        for (int i = 0; i < w.ia; i++) {
          Value cw = w.get(i);
          boolean b = false;
          for (Value v : xv) {
            if (v.eq(cw)) {
              b = true;
              break;
            }
          }
          res.add(b);
        }
      }
    }
    return res.finish();
  }
}