package BQN.types.callable.builtins.fns;

import BQN.errors.*;
import BQN.tools.*;
import BQN.types.Value;
import BQN.types.arrs.IntArr;
import BQN.types.callable.builtins.FnBuiltin;

import java.util.HashMap;

public class RBoxUBBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "⊒"; }
  
  public Value call(Value x) {
    if (x.r()==0) throw new DomainError("⊒: rank=0", this);
    if (x.r()!=1) throw new NYIError("⊒ on rank≠1", this);
    HashMap<Value, Integer> vs = new HashMap<Value, Integer>();
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
    if (x.r()!=1 || w.r()!=1) throw new NYIError("⊒ on rank≠1", this);
    HashMap<Value, MutIntArr> vs = new HashMap<Value, MutIntArr>();
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