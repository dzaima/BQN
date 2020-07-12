package APL.types.functions.builtins.fns2;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;

public class NotBuiltin extends Builtin {
  public String repr() {
    return "¬";
  }
  
  public Value call(Value x) {
    return rec(x);
  }
  
  public Value callInv(Value x) {
    return rec(x);
  }
  
  private Value rec(Value x) {
    if (x instanceof Arr) {
      ia: if (x.quickIntArr()) {
        int[] xi = x.asIntArr();
        int[] res = new int[xi.length];
        for (int i = 0; i < xi.length; i++) {
          int c = xi[i];
          if (c <= -2147483647) break ia; // bck: {x←•IA 𝕩 ⋄ x ≡○¬ 0+x}¨ bounds
          res[i] = 1-c;
        }
        return new IntArr(res, x.shape);
      }
      if (x instanceof BitArr) {
        BitArr wb = (BitArr) x;
        long[] res = new long[wb.arr.length];
        for (int i = 0; i < res.length; i++) res[i] = ~wb.arr[i];
        return new BitArr(res, x.shape);
      }
      
      if (x.quickDoubleArr()) {
        // for (int i = 0; i < w.length; i++) if (w[i] == 0) res[i>>6]|= 1L << (i&63);
        double[] ds = x.asDoubleArr();
        double[] res = new double[x.ia];
        for (int i = 0; i < ds.length; i++) {
          double v = ds[i];
          res[i] = 1 - v;
        }
        return new DoubleArr(res, x.shape);
      }
      
      Arr o = (Arr) x;
      Value[] arr = new Value[o.ia];
      for (int i = 0; i < o.ia; i++) {
        arr[i] = rec(o.get(i));
      }
      return new HArr(arr, o.shape);
    } else if (x instanceof Num) return Num.of(1-((Num) x).num);
    else throw new DomainError("Expected boolean, got "+x.humanType(false), this, x);
  }
  
  
  public static BitArr call(BitArr x) {
    BitArr.BC bc = BitArr.create(x.shape);
    for (int i = 0; i < bc.arr.length; i++) {
      bc.arr[i] = ~x.arr[i];
    }
    return bc.finish();
  }
  
  public Value call(Value w, Value x) {
    return PlusBuiltin.DF.call(MinusBuiltin.DF.call(w, x), Num.ONE);
  }
}