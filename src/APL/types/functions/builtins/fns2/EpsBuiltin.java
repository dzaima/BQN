package APL.types.functions.builtins.fns2;

import APL.errors.RankError;
import APL.types.*;
import APL.types.arrs.BitArr;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.mops.CellBuiltin;

import java.util.HashSet;

public class EpsBuiltin extends Builtin {
  public String repr() {
    return "∊";
  }
  
  public Value call(Value x) {
    if (x.rank == 0) throw new RankError("∊: argument cannot be scalar", this, x);
    Value[] vs;
    BitArr.BA res;
    if (x.rank == 1) {
      res = new BitArr.BA(x.shape);
      if (x.quickIntArr()) {
        HashSet<Integer> seen = new HashSet<>();
        for (int c : x.asIntArr()) res.add(seen.add(c));
        return res.finish();
      }
      vs = x.values();
    } else {
      res = new BitArr.BA(new int[]{x.shape[0]});
      vs = CellBuiltin.cells(x);
    }
    HashSet<Value> seen = new HashSet<>();
    for (Value c : vs) res.add(seen.add(c));
    return res.finish();
  }
  
  public Value call(Value w, Value x) {
    if (w.scalar()) { // TODO this _might_ be wrong
      Value w0 = w.first();
      for (Value v : x) {
        if (v.eq(w0)) {
          return Num.ONE;
        }
      }
      return Num.ZERO;
    }
    BitArr.BA res = new BitArr.BA(w.shape);
    if (w.ia>20 && x.ia>20) { // TODO these (and in ⊐) shouldn't be random numbers
      HashSet<Value> vs = new HashSet<>();
      for (Value c : x) vs.add(c);
      for (Value c : w) res.add(vs.contains(c));
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
    return res.finish();
  }
}