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
  
  private Value rec(Value w) {
    if (w instanceof Arr) {
      if (w instanceof BitArr) {
        BitArr wb = (BitArr) w;
        long[] res = new long[wb.llen()];
        for (int i = 0; i < res.length; i++) res[i] = ~wb.arr[i];
        return new BitArr(res, w.shape);
      }
      
      if (w.quickDoubleArr()) {
        // for (int i = 0; i < w.length; i++) if (w[i] == 0) res[i>>6]|= 1L << (i&63);
        double[] ds = w.asDoubleArr();
        double[] res = new double[w.ia];
        for (int i = 0; i < ds.length; i++) {
          double v = ds[i];
          res[i] = 1 - v;
        }
        return new DoubleArr(res, w.shape);
      }
      
      Arr o = (Arr) w;
      Value[] arr = new Value[o.ia];
      for (int i = 0; i < o.ia; i++) {
        arr[i] = rec(o.get(i));
      }
      return new HArr(arr, o.shape);
    } else if (w instanceof Num) return Num.of(1-((Num) w).num);
    else throw new DomainError("Expected boolean, got "+w.humanType(false), this, w);
  }
  
  
  public static BitArr call(BitArr w) {
    BitArr.BC bc = BitArr.create(w.shape);
    for (int i = 0; i < bc.arr.length; i++) {
      bc.arr[i] = ~w.arr[i];
    }
    return bc.finish();
  }
  
  public Value call(Value w, Value x) {
    return numD(PlusBuiltin.DNF, numD(MinusBuiltin.DNF, w, x), Num.ONE);
  }
}
