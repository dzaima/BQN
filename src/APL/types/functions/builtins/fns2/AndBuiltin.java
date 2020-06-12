package APL.types.functions.builtins.fns2;

import APL.types.*;
import APL.types.arrs.BitArr;
import APL.types.functions.Builtin;

import java.util.Arrays;

public class AndBuiltin extends Builtin {
  @Override public String repr() {
    return "∧";
  }
  
  
  
  public Value identity() {
    return Num.ONE;
  }
  
  public Value call(Value w) {
    var order = w.gradeUp();
    Value[] res = new Value[order.length];
    Arrays.setAll(res, i -> w.get(order[i]));
    return Arr.create(res);
  }
  
  private static final D_NNeN DNF = new D_NNeN() {
    public double on(double a, double w) {
      return Num.lcm2(a, w);
    }
    public void on(double[] res, double a, double[] w) {
      for (int i = 0; i < w.length; i++) res[i] = Num.lcm2(a, w[i]);
    }
    public void on(double[] res, double[] a, double w) {
      for (int i = 0; i < a.length; i++) res[i] = Num.lcm2(a[i], w);
    }
    public void on(double[] res, double[] a, double[] w) {
      for (int i = 0; i < a.length; i++) res[i] = Num.lcm2(a[i], w[i]);
    }
    public Value call(BigValue a, BigValue w) {
      return new BigValue(a.i.multiply(w.i).divide(a.i.gcd(w.i)));
    }
  };
  
  private static final D_BB DBF = new D_BB() {
    @Override public Value call(boolean a, BitArr w) {
      if (a) return w;
      return BitArr.fill(w, false);
    }
    @Override public Value call(BitArr a, boolean w) {
      if (w) return a;
      return BitArr.fill(a, false);
    }
    @Override public Value call(BitArr a, BitArr w) {
      BitArr.BC bc = new BitArr.BC(a.shape);
      for (int i = 0; i < a.arr.length; i++) bc.arr[i] = a.arr[i] & w.arr[i];
      return bc.finish();
    }
  };
  
  public Value call(Value a, Value w) {
    return bitD(DNF, DBF, a, w);
  }
}