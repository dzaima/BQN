package APL.types.functions.builtins.fns2;

import APL.types.*;
import APL.types.arrs.BitArr;
import APL.types.functions.Builtin;


public class NEBuiltin extends Builtin {
  @Override public String repr() {
    return "≠";
  }
  
  
  public Value call(Value x) {
    if (x.rank==0) return Num.ONE;
    return Num.of(x.shape[0]);
  }
  
  private static final D_NNeB DNF = new D_NNeB() {
    public boolean on(double w, double x) {
      return w != x;
    }
    public void on(BitArr.BA res, double w, double[] x) {
      for (double cw : x) res.add(w != cw);
    }
    public void on(BitArr.BA res, double[] w, double x) {
      for (double ca : w) res.add(ca != x);
    }
    public void on(BitArr.BA res, double[] w, double[] x) {
      for (int i = 0; i < w.length; i++) res.add(w[i] != x[i]);
    }
    public Value call(BigValue w, BigValue x) {
      return w.equals(x)? Num.ZERO : Num.ONE;
    }
  };
  private static final D_BB DBF = new D_BB() {
    @Override public Value call(boolean w, BitArr x) {
      if (w) return NotBuiltin.call(x);
      return x;
    }
    @Override public Value call(BitArr w, boolean x) {
      if (x) return NotBuiltin.call(w);
      return w;
    }
    @Override public Value call(BitArr w, BitArr x) {
      BitArr.BC bc = BitArr.create(x.shape);
      for (int i = 0; i < bc.arr.length; i++) bc.arr[i] = w.arr[i] ^ x.arr[i];
      return bc.finish();
    }
  };
  
  public Value call(Value w, Value x) {
    return ncbaD(DNF, DBF, (ca, cw) -> ca!=cw? Num.ONE : Num.ZERO, (ca, cw) -> ca.equals(cw)? Num.ZERO : Num.ONE, w, x);
  }
}