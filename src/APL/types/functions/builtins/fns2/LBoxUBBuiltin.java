package APL.types.functions.builtins.fns2;

import APL.Indexer;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;

public class LBoxUBBuiltin extends Builtin {
  public String repr() {
    return "⊑";
  }
  
  
  
  public Value call(Value w) {
    return w.first();
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
    if (a instanceof Primitive && w.rank==1) {
      Value r = w.get((int) a.asDouble());
      return new Rank0Arr(r);
    }
    
    return on(Indexer.poss(a, w.shape, blame), w);
  }
  
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
  
  
  public Value underW(Obj o, Value a, Value w) {
    Value v = o instanceof Fun? ((Fun) o).call(call(a, w)) : (Value) o;
    Value[] vs = w.valuesCopy();
    for (int i = 0; i < a.ia; i++) {
      vs[Indexer.fromShape(w.shape, a.get(i).asIntVec())] = v.get(i);
    }
    return Arr.create(vs, w.shape);
  }
}
