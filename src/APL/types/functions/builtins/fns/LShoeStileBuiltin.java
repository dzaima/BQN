package APL.types.functions.builtins.fns;

import APL.errors.RankError;
import APL.types.Value;
import APL.types.arrs.*;
import APL.types.functions.builtins.FnBuiltin;

import java.util.*;

public class LShoeStileBuiltin extends FnBuiltin {
  
  @Override public Value call(Value w, Value x) {
    HashMap<Value, Integer> counts = new HashMap<>();
    for (Value ca : w) counts.put(ca, 0);
    for (Value cw : x) {
      Integer pv = counts.get(cw);
      if (pv != null) counts.put(cw, pv + 1);
    }
    double[] res = new double[w.ia];
    int i = 0;
    for (Value ca : w) {
      res[i] = counts.get(ca);
      i++;
    }
    return new DoubleArr(res, w.shape);
  }
  
  @Override public Value call(Value x) {
    if (x.rank != 1) throw new RankError("⍧: rank of argument must be 1", this, x);
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
  
  @Override public String repr() {
    return "⍧";
  }
}