package APL.types.functions.builtins.fns;

import APL.Main;
import APL.errors.*;
import APL.tools.Indexer;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;

import java.util.HashMap;


public class IotaBuiltin extends Builtin {
  @Override public String repr() {
    return "⍳";
  }
  
  public Value call(Value x) {
    if (x instanceof Primitive) {
      if (x instanceof Num) {
        double[] res = new double[x.asInt()];
        for (int i = 0; i < res.length; i++) res[i] = i;
        return new DoubleArr(res);
      } else if (x instanceof BigValue) {
        Value[] res = new Value[x.asInt()];
        for (int i = 0; i < res.length; i++) {
          res[i] = new BigValue(i);
        }
        return new HArr(res);
      }
    }
    if (Main.vind) { // •VI←1
      if (x.rank != 1) throw new DomainError("⍳: 𝕩 must be a vector ("+Main.formatAPL(x.shape)+" ≡ ≢𝕩)", this, x);
      int dim = x.ia;
      int[] shape = x.asIntVec();
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
      int[] shape = x.asIntVec();
      int ia = Arr.prod(shape);
      Value[] arr = new Value[ia];
      int i = 0;
      for (int[] c : new Indexer(shape)) {
        arr[i] = new IntArr(c.clone());
        i++;
      }
      return new HArr(arr, shape);
    }
  }
  
  public Value call(Value w, Value x) {
    return on(w, x, this);
  }
  
  public static Value on(Value w, Value x, Callable blame) {
    if (x.rank > 1) throw new RankError("⍳: 𝕩 had rank > 1", blame, x);
    if (w.rank > 1) throw new RankError("⍳: 𝕨 had rank > 1", blame, w);
    if (x.ia > 20 && w.ia > 20) {
      HashMap<Value, Integer> map = new HashMap<>();
      int ctr = 0;
      for (Value v : w) {
        map.putIfAbsent(v, ctr);
        ctr++;
      }
      double[] res = new double[x.ia];
      ctr = 0;
      for (Value v : x) {
        Integer f = map.get(v);
        res[ctr] = f==null? w.ia : f;
        ctr++;
      }
      // w won't be a scalar
      return new DoubleArr(res, x.shape);
    }
    double[] res = new double[x.ia];
    int i = 0;
    for (Value cx : x) {
      int j = 0;
      for (Value cw : w) {
        if (cw.equals(cx)) break;
        j++;
      }
      res[i++] = j;
    }
    if (x instanceof Primitive) return new Num(res[0]);
    if (x.rank == 0) return new Num(res[0]);
    return new DoubleArr(res, x.shape);
  }
}