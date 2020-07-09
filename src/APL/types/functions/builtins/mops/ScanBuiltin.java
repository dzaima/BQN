package APL.types.functions.builtins.mops;

import APL.errors.*;
import APL.types.*;
import APL.types.arrs.DoubleArr;
import APL.types.functions.*;

public class ScanBuiltin extends Mop {
  @Override public String repr() {
    return "`";
  }
  
  public Value call(Value f, Value x, DerivedMop derv) {
    Fun ff = f.asFun();
    if (x.ia == 0) return x;
    if (x.rank == 0) throw new DomainError("`: rank must be at least 1, 𝕩 was a scalar", this, x);
    int l = x.ia / x.shape[0];
    Value c = x.get(0);
    Value[] res = new Value[x.ia];
    int i = 0;
    for (; i < l; i++) res[i] = x.get(i);
    for (; i < x.ia; i++) res[i] = ff.call(res[i-l], x.get(i));
    return Arr.create(res, x.shape);
  }
  
  public Value call(Value f, Value w, Value x, DerivedMop derv) {
    Fun ff = f.asFun();
    int n = w.asInt();
    int len = x.ia;
    if (n < 0) throw new DomainError("`: 𝕨 should be non-negative (was "+n+")", this);
    if (x.rank > 1) throw new RankError("`: rank of 𝕩 should be less than 2 (was "+x.rank+")", this);
    
    if (x.quickDoubleArr()) {
      Value[] res = new Value[len-n+1];
      double[] xa = x.asDoubleArr();
      for (int i = 0; i < res.length; i++) {
        double[] curr = new double[n];
        System.arraycopy(xa, i, curr, 0, n);
        res[i] = ff.call(new DoubleArr(curr));
      }
      return Arr.create(res);
    }
    
    Value[] res = new Value[len-n+1];
    Value[] xa = x.values();
    for (int i = 0; i < res.length; i++) {
      Value[] curr = new Value[n];
      // for (int j = 0; j < n; j++) curr[j] = wa[i + j];
      System.arraycopy(xa, i, curr, 0, n);
      res[i] = ff.call(Arr.create(curr));
    }
    return Arr.create(res);
  }
}
