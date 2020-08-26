package APL.types.functions.builtins.fns;

import APL.Main;
import APL.errors.*;
import APL.types.*;
import APL.types.functions.Builtin;

public class RShoeBuiltin extends Builtin {
  @Override public String repr() {
    return "⊃";
  }
  
  public Value call(Value x) {
    if (x instanceof Primitive) return x;
    else if (x.ia == 0) return x.prototype();
    else return x.first();
  }
  
  public Value call(Value w, Value x) {
    if (x instanceof APLMap) {
      APLMap map = (APLMap) x;
      return map.getRaw(w);
    }
    if (w instanceof Num) {
      if (x.rank != 1) throw new RankError("array rank was "+x.rank+", tried to get item at rank 0", this, x);
      if (x.ia == 0) throw new LengthError("⊃ on array with 0 elements", this, x);
      int p = w.asInt();
      if (p >= x.ia) throw new DomainError("Tried to access item at position "+w+" while shape was "+Main.formatAPL(x.shape), this);
      return x.get(p);
    }
    for (Value v : w) {
      x = x.at(v.asIntVec());
    }
    return x;
  }
  
  public Value under(Value o, Value x) {
    Value[] vs = x.valuesClone();
    vs[0] = o instanceof Fun? o.call(call(x)) : o;
    return Arr.create(vs, x.shape);
  }
  
  public Value underW(Value o, Value w, Value x) {
    Value v = o instanceof Fun? o.call(call(w, x)) : o;
    if (w instanceof Primitive) {
      Value[] vs = x.valuesClone();
      vs[w.asInt()] = v;
      return Arr.create(vs, x.shape);
    } else {
      Value[] vs = x.valuesClone();
      int[] is = w.asIntVec();
      replace(vs, v, is, 0);
      return Arr.create(vs, x.shape);
    }
  }
  private void replace(Value[] vs, Value w, int[] d, int i) {
    int c = d[i];
    if (i+1 == d.length) vs[c] = w;
    else {
      Value cv = vs[c];
      Value[] vsN = cv.valuesClone();
      replace(vsN, w, d, i+1);
      vs[c] = Arr.create(vsN, cv.shape);
    }
  }
}