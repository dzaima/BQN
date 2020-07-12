package APL.types.functions.builtins.fns;

import APL.Main;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.fns2.NotBuiltin;

public class NorBuiltin extends Builtin {
  @Override public String repr() {
    return "⍱";
  }
  
  private static final D_NNeN DNF = new D_NNeN() {
    public double on(double w, double x) {
      return Main.bool(w)|Main.bool(x)? 0 : 1;
    }
    public void on(double[] res, double w, double[] x) {
      for (int i = 0; i < x.length; i++) res[i] = Main.bool(w)|Main.bool(x[i])? 0 : 1;
    }
    public void on(double[] res, double[] w, double x) {
      for (int i = 0; i < w.length; i++) res[i] = Main.bool(w[i])|Main.bool(x)? 0 : 1;
    }
    public void on(double[] res, double[] w, double[] x) {
      for (int i = 0; i < w.length; i++) res[i] = Main.bool(w[i])|Main.bool(x[i])? 0 : 1;
    }
  };
  
  private static final D_BB DBF = new D_BB() {
    @Override public Value call(boolean w, BitArr x) {
      if (w) return BitArr.fill(x, false);
      return NotBuiltin.call(x);
    }
    @Override public Value call(BitArr w, boolean x) {
      if (x) return BitArr.fill(w, false);
      return NotBuiltin.call(w);
    }
    @Override public Value call(BitArr w, BitArr x) {
      BitArr.BC bc = new BitArr.BC(w.shape);
      for (int i = 0; i < w.arr.length; i++) bc.arr[i] = ~(w.arr[i] | x.arr[i]);
      return bc.finish();
    }
  };
  
  public Value call(Value w, Value x) {
    return bitD(DNF, DBF, w, x);
  }
  public Value call(Value x) {
    if (x instanceof BitArr) {
      BitArr wb = (BitArr) x;
      wb.setEnd(false);
      for (long l : wb.arr) if (l != 0L) return Num.ZERO;
      return Num.ONE;
    }
    if (x.quickDoubleArr()) {
      double[] da = x.asDoubleArr();
      for (int i = 0; i < x.ia; i++) {
        if (Main.bool(da[i])) return Num.ZERO;
      }
      return Num.ONE;
    }
    for (int i = 0; i < x.ia; i++) {
      if (Main.bool(x.get(i))) return Num.ZERO;
    }
    return Num.ONE;
  }
}