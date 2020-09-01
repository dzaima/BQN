package APL.types.functions.builtins.fns2;

import APL.tools.Indexer;
import APL.types.*;
import APL.types.arrs.DoubleArr;
import APL.types.functions.Builtin;

public class TransposeBuiltin extends Builtin {
  @Override public String repr() {
    return "⍉";
  }
  
  public Value call(Value x) {
    if (x.scalar()) return x;
    int r = x.rank;
    int[] sh = new int[r];
    int n = 1;
    for (int i = 0; i < r-1; i++) {
      n *= sh[i] = x.shape[i + 1];
    }
    int m = sh[r - 1] = x.shape[0];
    return matTrans(x, m, n, sh);
  }
  public Value callInv(Value x) {
    if (x.scalar()) return x;
    int r = x.rank;
    int[] sh = new int[r];
    int n = sh[0] = x.shape[r - 1];
    int m = 1;
    for (int i = 1; i < r; i++) {
      m *= sh[i] = x.shape[i - 1];
    }
    return matTrans(x, m, n, sh);
  }
  
  static Value matTrans(Value x, int m, int n, int[] sh) {
    if (m==0 || n==0) return x.ofShape(sh);
    if (x instanceof DoubleArr) {
      double[] dw = x.asDoubleArr();
      double[] res = new double[x.ia];
      int ip = 0;
      for (int cx = 0; cx < m; cx++) {
        int op = cx;
        for (int cy = 0; cy < n; cy++) {
          res[op] = dw[ip++];
          op += m;
        }
      }
      return new DoubleArr(res, sh);
    } else {
      Value[] res = new Value[x.ia];
      int ip = 0;
      for (int cx = 0; cx < m; cx++) {
        int op = cx;
        for (int cy = 0; cy < n; cy++) {
          res[op] = x.get(ip++);
          op += m;
        }
      }
      return Arr.create(res, sh);
    }
  }
}
