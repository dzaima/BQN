package APL.types.callable.builtins.fns;

import APL.Main;
import APL.errors.DomainError;
import APL.types.*;
import APL.types.arrs.DoubleArr;
import APL.types.callable.builtins.FnBuiltin;

import java.util.Arrays;

public class MergeBuiltin extends FnBuiltin {
  
  @Override public Value call(Value w, Value x) {
    if (x.rank != 1) throw new DomainError("%: 𝕩 must be a vector", this, x);
    int[] sh = w.shape;
    int i1 = 0;
    boolean allds = true;
    for (Value v : x) {
      if (!Arrays.equals(v.shape, sh)) throw new DomainError("%: shape of item "+i1+" in 𝕩 didn't match 𝕨 ("+Main.formatAPL(sh)+" vs "+Main.formatAPL(v.shape)+")", this, x);
      i1++;
      if (!v.quickDoubleArr()) allds = false;
    }
    // if (IO==0 && a instanceof BitArr) { TODO
    //   
    // }
    if (allds) {
      double[] ds = new double[w.ia];
      double[][] wds = new double[x.ia][];
      for (int i = 0; i < x.ia; i++) wds[i] = x.get(i).asDoubleArr();
      int[] idx = w.asIntArr();
      for (int i = 0; i < idx.length; i++) {
        ds[i] = wds[idx[i]][i];
      }
      if (w.rank == 0) return new Num(ds[0]);
      return new DoubleArr(ds, w.shape);
    }
    Value[] vs = new Value[w.ia];
    int[] idx = w.asIntArr();
    for (int i = 0; i < idx.length; i++) {
      vs[i] = x.get(idx[i]).get(i);
    }
    return Arr.create(vs, w.shape);
  }
  
  @Override public String repr() {
    return "%";
  }
}