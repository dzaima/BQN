package APL.types.callable.builtins.fns;

import APL.Main;
import APL.errors.*;
import APL.tools.*;
import APL.types.*;
import APL.types.arrs.DoubleArr;
import APL.types.callable.builtins.FnBuiltin;
import APL.types.callable.builtins.md2.AtBuiltin;

import java.util.Arrays;

public class LBoxUBBuiltin extends FnBuiltin {
  public String repr() {
    return "⊑";
  }
  
  
  
  public Value call(Value x) {
    if (x.ia == 0) return x.prototype();
    return x.first();
  }
  
  public Value under(Value o, Value x) {
    if (x.ia == 0) throw new LengthError("⌾⊑: called on empty array", this, x);
    Value v = o instanceof Fun? o.call(call(x)) : o;
    MutVal m = new MutVal(x.shape);
    m.copy(x, 1, 1, x.ia-1);
    m.set(0, v);
    return m.get();
  }
  
  
  
  public Value call(Value w, Value x) {
    return on(w, x, this);
  }
  
  public static Value on(Value w, Value x, Callable blame) {
    if (x instanceof APLMap) {
      Value[] res = new Value[w.ia];
      APLMap map = (APLMap) x;
      Value[] vs = w.values();
      for (int i = 0; i < w.ia; i++) {
        res[i] = map.getRaw(vs[i].asString());
      }
      return Arr.create(res, w.shape);
    }
    if (w instanceof Primitive) {
      return x.get(Indexer.scal(w.asInt(), x.shape, blame));
    } else {
      if (Main.vind) return on(Indexer.poss(w, x.shape, blame), x);
      return onArr(w, x, blame);
    }
  }
  
  static Value onArr(Value w, Value x, Callable blame) {
    if (w instanceof Primitive) throw new DomainError(blame+": indices must all be vectors when nesting (found "+w+")", blame);
    if (w.ia>=1 && !(w.first() instanceof Primitive)) {
      Value[] vs = new Value[w.ia];
      for (int i = 0; i < w.ia; i++) vs[i] = onArr(w.get(i), x, blame);
      return Arr.create(vs, w.shape);
    }
    return x.get(Indexer.vec(w, x.shape, blame));
  }
  
  public static Value on(Indexer.PosSh poss, Value x) {
    if (x.quickDoubleArr()) {
      double[] res = new double[Arr.prod(poss.sh)];
      double[] xd = x.asDoubleArr();
      int[] idxs = poss.vals;
      for (int i = 0; i < idxs.length; i++) {
        res[i] = xd[idxs[i]];
      }
      return new DoubleArr(res, poss.sh);
    }
    Value[] res = new Value[Arr.prod(poss.sh)];
    int[] idxs = poss.vals;
    for (int i = 0; i < idxs.length; i++) {
      res[i] = x.get(idxs[i]);
    }
    return Arr.create(res, poss.sh);
  }
  
  
  public Value underW(Value o, Value w, Value x) {
    Value v = o instanceof Fun? o.call(call(w, x)) : o;
    
    if (Main.vind) {
      return AtBuiltin.with(x, Indexer.poss(w, x.shape, this), v, this);
    }
    
    Value[] vs = x.valuesClone();
    if (w instanceof Primitive) vs[Indexer.scal(w.asInt(), x.shape, this)] = v;
    else underWSub(v, w, vs, x.shape);
    
    return Arr.create(vs, x.shape);
  }
  
  void underWSub(Value v, Value a, Value[] vs, int[] shape) {
    if (a instanceof Primitive) throw new DomainError(this+": indices must all be vectors when nesting (found "+a+")", this);
    if (a.ia>=1 && !(a.get(0) instanceof Primitive)) {
      if (a.r() != v.r()) throw new RankError(this+": shapes of nested indices and values must be equal (ranks "+a.r()+" vs "+v.r()+")", this);
      if (!Arrays.equals(a.shape, v.shape)) throw new LengthError(this+": shapes of nested indices and values must be equal ("+Main.formatAPL(a.shape)+" vs "+Main.formatAPL(v.shape)+")", this);
      for (int i = 0; i < a.ia; i++) underWSub(v.get(i), a.get(i), vs, shape);
    } else {
      vs[Indexer.vec(a, shape, this)] = v;
    }
  }
}