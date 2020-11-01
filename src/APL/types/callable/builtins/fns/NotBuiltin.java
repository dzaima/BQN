package APL.types.callable.builtins.fns;

import APL.errors.DomainError;
import APL.tools.FmtInfo;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.callable.builtins.FnBuiltin;

public class NotBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "¬"; }
  
  public Value call(Value x) {
    return on(x, this);
  }
  public Value callInv(Value x) {
    return on(x, this);
  }
  
  public static Value on(Value x, Callable blame) {
    if (x instanceof Arr) {
      if (x instanceof BitArr) {
        BitArr xb = (BitArr) x;
        long[] res = new long[xb.arr.length];
        for (int i = 0; i < res.length; i++) res[i] = ~xb.arr[i];
        return new BitArr(res, x.shape);
      }
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
      
      if (x.quickDoubleArr()) {
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
        arr[i] = on(o.get(i), blame);
      }
      return new HArr(arr, o.shape);
    } else if (x instanceof Num) return Num.of(1-((Num) x).num);
    else throw new DomainError("Expected boolean, got "+x.humanType(false), blame, x);
  }
  
  public static BitArr on(BitArr x) {
    BitArr.BC res = BitArr.create(x.shape);
    for (int i = 0; i < res.arr.length; i++) res.arr[i] = ~x.arr[i];
    return res.finish();
  }
  
  public Value call(Value w, Value x) {
    return PlusBuiltin.DF.call(MinusBuiltin.DF.call(w, x), Num.ONE);
  }
  public Value callInvX(Value w, Value x) {
    return call(w, x);
  }
  public Value callInvW(Value w, Value x) {
    return MinusBuiltin.DF.scalarX(PlusBuiltin.DF.call(w,x), 1);
  }
}