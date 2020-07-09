package APL.types.functions.builtins.fns;

import APL.errors.*;
import APL.types.*;
import APL.types.functions.Builtin;

public class  ExpandBuiltin extends Builtin {
  public String repr() {
    return "⍀";
  }
  
  public Value call(Value w, Value x) {
    if (w.rank != 1) throw new RankError("⍀: 𝕨 bust be of rank 1", this, w);
    if (x.rank >= 2) throw new NYIError("⍀: rank 2 or more 𝕩", this, x);
    Value pr = null;
    int[] is = w.asIntArr(); // vectorness checked before
    int ram = 0;
    int iam = 0;
    for (int v : is) {
      ram+= Math.max(1, Math.abs(v));
      iam+= v>0? 1 : 0;
    }
    if (iam != x.ia) throw new DomainError("⍀: required input amount ("+iam+") not equal to given ("+x.ia+")", this);
    Value[] res = new Value[ram];
    int rp = 0;
    int ip = 0;
    
    for (int v : is) {
      if (v <= 0) {
        if (pr == null) pr = x.safePrototype();
        v = Math.max(1, -v);
        for (int i = 0; i < v; i++) res[rp++] = pr;
      } else {
        Value c = x.get(ip);
        for (int i = 0; i < v; i++) {
          res[rp++] = c;
        }
        ip++;
      }
    }
    
    return Arr.create(res);
  }
}