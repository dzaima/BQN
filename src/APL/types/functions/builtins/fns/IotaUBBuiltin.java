package APL.types.functions.builtins.fns;

import APL.*;
import APL.errors.DomainError;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.fns2.CeilingBuiltin;
import APL.types.functions.builtins.mops.ReduceBuiltin;

public class IotaUBBuiltin extends Builtin {
  private static final Fun fn = new ReduceBuiltin().derive(new CeilingBuiltin());
  @Override public String repr() {
    return "⍸";
  }
  
  public Value call(Value x) {
    int sum = (int) x.sum();
    if (x.rank == 1) {
      if (sum<0) {
        for (Value v : x) if (v.asDouble() < 0) throw new DomainError("⍸: 𝕩 contained "+v, this, x);
      }
      var sub = new double[sum];
      int p = 0;
      
      if (x instanceof BitArr) {
        BitArr.BR r = ((BitArr) x).read();
        for (int i = 0; i < x.ia; i++) {
          if (r.read()) sub[p++] = i;
        }
      } else {
        var da = x.asDoubleArr();
        for (int i = 0; i < x.ia; i++) {
          int v = (int) da[i];
          if (v < 0) throw new DomainError("⍸: 𝕩 contained "+v, this, x);
          for (int j = 0; j < v; j++) {
            sub[p++] = i;
          }
        }
      }
      return new DoubleArr(sub);
    } else {
      double[] wd = x.asDoubleArr();
      if (Main.vind) { // •VI←1
        double[][] res = new double[x.rank][sum];
        int ri = 0;
        Indexer idx = new Indexer(x.shape);
        int rank = res.length;
        for (int i = 0; i < x.ia; i++) {
          int[] p = idx.next();
          int n = Num.toInt(wd[idx.pos()]);
          if (n > 0) {
            for (int k = 0; k < rank; k++) {
              for (int j = 0; j < n; j++) res[k][ri+j] = p[k];
            }
            ri+= n;
          } else if (n != 0) throw new DomainError("⍸: 𝕩 contained "+n, this, x);
        }
        Value[] resv = new Value[rank];
        for (int i = 0; i < rank; i++) resv[i] = new DoubleArr(res[i]);
        return new HArr(resv);
      } else { // •VI←0
        Value[] res = new Value[sum];
        int ri = 0;
        Indexer idx = new Indexer(x.shape);
        for (int i = 0; i < x.ia; i++) {
          int[] p = idx.next();
          int n = Num.toInt(wd[idx.pos()]);
          if (n > 0) {
            DoubleArr pos = Main.toAPL(p);
            for (int j = 0; j < n; j++) res[ri++] = pos;
          } else if (n != 0) throw new DomainError("⍸: 𝕩 contained "+n, this, x);
        }
        return new HArr(res);
      }
    }
  }
  public Value callInv(Value x) {
    int[] sh = fn.call(x).asIntVec();
    int ia = 1;
    for (int i = 0; i < sh.length; i++) {
      sh[i]+= 1;
      ia*= sh[i];
    }
    double[] arr = new double[ia];
    for (Value v : x) {
      int[] c = v.asIntVec();
      arr[Indexer.fromShape(sh, c)]++;
    }
    return new DoubleArr(arr, sh);
  }
}