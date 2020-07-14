package APL.types.functions.builtins.fns2;

import APL.algs.MutIntArr;
import APL.errors.*;
import APL.types.Value;
import APL.types.arrs.IntArr;
import APL.types.functions.Builtin;

import java.util.HashMap;

public class RBoxUBBuiltin extends Builtin {
  public String repr() {
    return "⊒";
  }
  
  public Value call(Value x) {
    if (x.rank==0) throw new DomainError("⊒: rank=0", this, x);
    if (x.rank!=1) throw new NYIError("⊒ on rank≠1", this, x);
    HashMap<Value, Integer> vs = new HashMap<>();
    int[] res = new int[x.ia];
    int i = 0;
    for (Value v : x) {
      Integer c = vs.get(v);
      if (c==null) {
        vs.put(v, 1);
      } else {
        res[i] = c;
        vs.put(v, c+1);
      }
      i++;
    }
    return new IntArr(res);
  }
  
  public Value call(Value w, Value x) {
    if (x.rank!=1 || w.rank!=1) throw new NYIError("⊒ on rank≠1", this, w);
    HashMap<Value, MutIntArr> vs = new HashMap<>();
    int i = 0;
    for (Value v : w) {
      MutIntArr c = vs.get(v);
      if (c==null) {
        c = new MutIntArr(2);
        vs.put(v, c);
      }
      c.add(i);
      i++;
    }
    int[] res = new int[x.ia];
    i=0;
    for (Value v : x) {
      MutIntArr c = vs.get(v);
      if (c==null || c.pos >= c.sz) res[i] = w.ia;
      else res[i] = c.is[c.pos++];
      i++;
    }
    return new IntArr(res);
  }
  
}