package APL.types.functions.builtins.fns2;

import APL.errors.NYIError;
import APL.types.*;
import APL.types.arrs.BitArr;
import APL.types.functions.Builtin;

import java.util.HashSet;

public class EpsBuiltin extends Builtin {
  public String repr() {
    return "∊";
  }
  
  public Value call(Value x) {
    if (x.rank != 1) throw new NYIError("∊: rank of argument must be 1", this, x); // TODO
    HashSet<Value> encountered = new HashSet<>();
    BitArr.BA res = new BitArr.BA(x.shape);
    for (Value cv : x) {
      if (encountered.contains(cv)) res.add(false);
      else {
        encountered.add(cv);
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