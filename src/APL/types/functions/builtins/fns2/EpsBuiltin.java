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
      vs = x.values();
      res = new BitArr.BA(x.shape);
    } else {
      vs = CellBuiltin.cells(x);
      res = new BitArr.BA(new int[]{x.shape[0]});
    }
    HashSet<Value> encountered = new HashSet<>();
    
    for (Value v : vs) {
      if (encountered.contains(v)) {
        res.add(false);
      } else {
        encountered.add(v);
        res.add(true);
      }
    }
    return res.finish();
  }
  
  public Value call(Value w, Value x) {
    if (w.scalar()) {
      Value w0 = w.first();
      for (Value v : x) {
        if (v.equals(w0)) {
          return Num.ONE;
        }
      }
      return Num.ZERO;
    }
    BitArr.BA ba = new BitArr.BA(w.shape);
    for (int i = 0; i < w.ia; i++) {
      Value cw = w.get(i);
      boolean b = false;
      for (Value v : x) {
        if (v.equals(cw)) {
          b = true;
          break;
        }
      }
      ba.add(b);
    }
    return ba.finish();
  }
}