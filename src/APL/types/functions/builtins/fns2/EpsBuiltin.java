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
  
  public Value call(Value w) {
    if (w.rank != 1) throw new NYIError("∊: rank of argument must be 1", this, w); // TODO
    HashSet<Value> encountered = new HashSet<>();
    BitArr.BA res = new BitArr.BA(w.shape);
    for (Value cv : w) {
      if (encountered.contains(cv)) res.add(false);
      else {
        encountered.add(cv);
        res.add(true);
      }
    }
    return res.finish();
  }
  
  public Value call(Value a, Value w) {
    if (a.scalar()) {
      Value a1 = a.first();
      for (Value v : w) {
        if (v.equals(a1)) {
          return Num.ONE;
        }
      }
      return Num.ZERO;
    }
    BitArr.BA ba = new BitArr.BA(a.shape);
    for (int i = 0; i < a.ia; i++) {
      Value av = a.get(i);
      boolean b = false;
      for (Value v : w) {
        if (v.equals(av)) {
          b = true;
          break;
        }
      }
      ba.add(b);
    }
    return ba.finish();
  }
}
