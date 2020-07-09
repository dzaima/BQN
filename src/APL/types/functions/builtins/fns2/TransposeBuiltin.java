package APL.types.functions.builtins.fns2;

import APL.Indexer;
import APL.types.*;
import APL.types.arrs.DoubleArr;
import APL.types.functions.Builtin;

public class TransposeBuiltin extends Builtin {
  @Override public String repr() {
    return "⍉";
  }
  
  
  
  public Value call(Value x) {
    if (x.scalar()) return x;
    if (x instanceof DoubleArr) {
      double[] dw = x.asDoubleArr();
      double[] res = new double[x.ia];
      int[] sh = new int[x.rank];
      for (int i = 0; i < x.rank; i++) {
        sh[i] = x.shape[x.rank - i - 1];
      }
      if (x.rank == 2) {
        int ww = x.shape[0];
        int wh = x.shape[1];
        int ip = 0;
        for (int cx = 0; cx < ww; cx++) {
          int op = cx;
          for (int cy = 0; cy < wh; cy++) {
            res[op] = dw[ip++];
            op+= ww;
          }
        }
      } else {
        int ci = 0;
        for (int[] c : new Indexer(x.shape)) {
          int[] nc = new int[x.rank];
          for (int i = 0; i < x.rank; i++) {
            nc[i] = c[x.rank - i - 1];
          }
          res[Indexer.fromShape(sh, nc)] = dw[ci++];
        }
      }
      return new DoubleArr(res, sh);
    }
    Value[] arr = new Value[x.ia];
    int[] ns = new int[x.rank];
    for (int i = 0; i < x.rank; i++) {
      ns[i] = x.shape[x.rank - i - 1];
    }
    for (int[] c : new Indexer(x.shape)) {
      int[] nc = new int[x.rank];
      for (int i = 0; i < x.rank; i++) {
        nc[i] = c[x.rank - i - 1];
      }
      arr[Indexer.fromShape(ns, nc)] = x.simpleAt(c);
    }
    return Arr.create(arr, ns);
  }
  public Value callInv(Value x) {
    return call(x);
  }
}