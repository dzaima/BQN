package APL.types.functions.builtins.fns;

import APL.*;
import APL.errors.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;

import java.util.HashMap;


public class IotaBuiltin extends Builtin {
  @Override public String repr() {
    return "⍳";
  }
  
  public Value call(Value w) {
    if (w instanceof Primitive) {
      if (w instanceof Num) {
        double[] res = new double[w.asInt()];
        for (int i = 0; i < res.length; i++) res[i] = i;
        return new DoubleArr(res);
      } else if (w instanceof BigValue) {
        Value[] res = new Value[w.asInt()];
        for (int i = 0; i < res.length; i++) {
          res[i] = new BigValue(i);
        }
        return new HArr(res);
      }
    }
    if (Main.vind) { // •VI←1
      if (w.rank != 1) throw new DomainError("⍳: 𝕩 must be a vector ("+ Main.formatAPL(w.shape)+" ≡ ≢𝕩)", this, w);
      int dim = w.ia;
      int[] shape = w.asIntVec();
      int prod = Arr.prod(shape);
      Value[] res = new Value[dim];
      int blockSize = 1;
      for (int i = dim-1; i >= 0; i--) {
        double[] ds = new double[prod];
        int len = shape[i];
        int csz = 0;
        double val = 0;
        for (int k = 0; k < len; k++) {
          for (int l = 0; l < blockSize; l++) ds[csz++] = val;
          val++;
        }
        int j = csz;
        while (j < prod) {
          System.arraycopy(ds, 0, ds, j, csz);
          j+= csz;
        }
        res[i] = new DoubleArr(ds, shape);
        blockSize*= shape[i];
      }
      return new HArr(res);
    } else { // •VI←0
      int[] shape = w.asIntVec();
      int ia = Arr.prod(shape);
      Value[] arr = new Value[ia];
      int i = 0;
      for (int[] c : new Indexer(shape)) {
        arr[i] = Main.toAPL(c);
        i++;
      }
      return new HArr(arr, shape);
    }
  }
  
  public Value call(Value a, Value w) {
    return on(a, w, this);
  }
  
  public static Value on(Value a, Value w, Callable blame) {
    if (w.rank > 1) throw new RankError("⍳: 𝕩 had rank > 1", blame, w);
    if (a.rank > 1) throw new RankError("⍳: 𝕨 had rank > 1", blame, a);
    if (w.ia > 20 && a.ia > 20) {
      HashMap<Value, Integer> map = new HashMap<>();
      int ctr = 0;
      for (Value v : a) {
        map.putIfAbsent(v, ctr);
        ctr++;
      }
      double[] res = new double[w.ia];
      ctr = 0;
      for (Value v : w) {
        Integer f = map.get(v);
        res[ctr] = f==null? a.ia : f;
        ctr++;
      }
      // w won't be a scalar
      return new DoubleArr(res, w.shape);
    }
    double[] res = new double[w.ia];
    int i = 0;
    for (Value wv : w) {
      int j = 0;
      for (Value av : a) {
        if (av.equals(wv)) break;
        j++;
      }
      res[i++] = j;
    }
    if (w instanceof Primitive) return new Num(res[0]);
    if (w.rank == 0) return new Num(res[0]);
    return new DoubleArr(res, w.shape);
  }
}