package APL.types.functions.builtins.fns2;

import APL.*;
import APL.errors.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;

import java.util.Arrays;

public class LBoxUBBuiltin extends Builtin {
  public String repr() {
    return "⊑";
  }
  
  
  
  public Value call(Value x) {
    return x.first();
  }
  
  public Value under(Value o, Value w) {
    if (w.ia == 0) throw new LengthError("⌾⊑: called on empty array", this, w);
    Value v = o instanceof Fun? ((Fun) o).call(call(w)) : o;
    Value[] vs = w.valuesCopy();
    vs[0] = v;
    return HArr.create(vs, w.shape);
  }
  
  
  
  public Value call(Value a, Value w) {
    return on(a, w, this);
  }
  
  public static Value on(Value a, Value w, Callable blame) {
    if (w instanceof APLMap) {
      Value[] res = new Value[a.ia];
      APLMap map = (APLMap) w;
      Value[] vs = a.values();
      for (int i = 0; i < a.ia; i++) {
        res[i] = map.getRaw(vs[i].asString());
      }
      return Arr.create(res, a.shape);
    }
    if (a instanceof Primitive) {
      return w.get(Indexer.scal(a.asInt(), w.shape, blame));
    } else {
      return onArr(a, w, blame);
    }
  }
  
  static Value onArr(Value a, Value w, Callable blame) {
    if (a instanceof Primitive) throw new DomainError(blame+": indices must all be vectors when nesting (found "+a+")", blame);
    if (a.ia>=1 && !(a.get(0) instanceof Primitive)) {
      Value[] vs = new Value[a.ia];
      for (int i = 0; i < a.ia; i++) vs[i] = onArr(a.get(i), w, blame);
      return Arr.create(vs, a.shape);
    }
    return w.get(Indexer.vec(a, w.shape, blame));
  }
  
  // only used by AtBuiltin
  public static Value on(Indexer.PosSh poss, Value w) {
    if (w.quickDoubleArr()) {
      double[] res = new double[Arr.prod(poss.sh)];
      double[] wd = w.asDoubleArr();
      int[] idxs = poss.vals;
      for (int i = 0; i < idxs.length; i++) {
        res[i] = wd[idxs[i]];
      }
      return new DoubleArr(res, poss.sh);
    }
    Value[] res = new Value[Arr.prod(poss.sh)];
    int[] idxs = poss.vals;
    for (int i = 0; i < idxs.length; i++) {
      res[i] = w.get(idxs[i]);
    }
    return Arr.create(res, poss.sh);
  }
  
  
  public Value underW(Value o, Value a, Value w) {
    Value v = o instanceof Fun? ((Fun) o).call(call(a, w)) : o;
    Value[] vs = w.valuesCopy();
    if (a instanceof Primitive) {
      vs[Indexer.scal(a.asInt(), w.shape, this)] = v;
    } else {
      underWSub(v, a, vs, w.shape);
    }

    return Arr.create(vs, w.shape);
  }

  void underWSub(Value v, Value a, Value[] vs, int[] shape) {
    if (a instanceof Primitive) throw new DomainError(this+": indices must all be vectors when nesting (found "+a+")", this);
    if (a.ia>=1 && !(a.get(0) instanceof Primitive)) {
      if (a.rank != v.rank) throw new RankError(this+": shapes of nested indices and values must be equal (ranks "+a.rank+" vs "+v.rank + ")", this);
      if (!Arrays.equals(a.shape, v.shape)) throw new LengthError(this+": shapes of nested indices and values must be equal ("+ Main.formatAPL(a.shape) + " vs " + Main.formatAPL(v.shape) + ")", this);
      for (int i = 0; i < a.ia; i++) underWSub(v.get(i), a.get(i), vs, shape);
    } else {
      vs[Indexer.vec(a, shape, this)] = v;
    }
  }
}
