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
  
  public Value under(Value o, Value x) {
    if (x.ia == 0) throw new LengthError("⌾⊑: called on empty array", this, x);
    Value v = o instanceof Fun? ((Fun) o).call(call(x)) : o;
    Value[] vs = x.valuesCopy();
    vs[0] = v;
    return HArr.create(vs, x.shape);
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
      return onArr(w, x, blame);
    }
  }
  
  static Value onArr(Value w, Value x, Callable blame) {
    if (w instanceof Primitive) throw new DomainError(blame+": indices must all be vectors when nesting (found "+w+")", blame);
    if (w.ia>=1 && !(w.get(0) instanceof Primitive)) {
      Value[] vs = new Value[w.ia];
      for (int i = 0; i < w.ia; i++) vs[i] = onArr(w.get(i), x, blame);
      return Arr.create(vs, w.shape);
    }
    return x.get(Indexer.vec(w, x.shape, blame));
  }
  
  // only used by AtBuiltin
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
    Value v = o instanceof Fun? ((Fun) o).call(call(w, x)) : o;
    Value[] vs = x.valuesCopy();
    if (w instanceof Primitive) {
      vs[Indexer.scal(w.asInt(), x.shape, this)] = v;
    } else {
      underWSub(v, w, vs, x.shape);
    }

    return Arr.create(vs, x.shape);
  }

  void underWSub(Value v, Value a, Value[] vs, int[] shape) {
    if (a instanceof Primitive) throw new DomainError(this+": indices must all be vectors when nesting (found "+a+")", this);
    if (a.ia>=1 && !(a.get(0) instanceof Primitive)) {
      if (a.rank != v.rank) throw new RankError(this+": shapes of nested indices and values must be equal (ranks "+a.rank+" vs "+v.rank + ")", this);
      if (!Arrays.equals(a.shape, v.shape)) throw new LengthError(this+": shapes of nested indices and values must be equal ("+Main.formatAPL(a.shape)+" vs "+Main.formatAPL(v.shape)+")", this);
      for (int i = 0; i < a.ia; i++) underWSub(v.get(i), a.get(i), vs, shape);
    } else {
      vs[Indexer.vec(a, shape, this)] = v;
    }
  }
}
