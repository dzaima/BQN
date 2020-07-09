package APL.types.functions.builtins.fns2;

import APL.errors.RankError;
import APL.types.*;
import APL.types.arrs.BitArr;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.mops.CellBuiltin;

public class OrBuiltin extends Builtin {
  @Override public String repr() {
    return "∨";
  }
  
  
  
  public Value identity() {
    return Num.ZERO;
  }
  
  public Value call(Value x) {
    if (x.rank==0) throw new RankError("∨: argument cannot be scalar", this, x);
    Integer[] order = x.gradeDown();
    Value[] vs = x.values();
    Value[] res = new Value[x.ia];
    int csz = CellBuiltin.csz(x);
    for (int i = 0; i < order.length; i++) System.arraycopy(vs, order[i]*csz, res, i*csz, csz);
    return Arr.create(res, x.shape);
  }
  
  private static final D_NNeN DNF = new D_NNeN() {
    public double on(double w, double x) {
      return w+x - w*x;
    }
    public void on(double[] res, double w, double[] x) {
      for (int i = 0; i < x.length; i++) res[i] = w+x[i] - w*x[i];
    }
    public void on(double[] res, double[] w, double x) {
      for (int i = 0; i < w.length; i++) res[i] = w[i]+x - w[i]*x;
    }
    public void on(double[] res, double[] w, double[] x) {
      for (int i = 0; i < w.length; i++) res[i] = w[i]+x[i] - w[i]*x[i];
    }
    public Value call(BigValue w, BigValue x) {
      return new BigValue(w.i.gcd(x.i));
    }
  };
  
  private static final D_BB DBF = new D_BB() {
    @Override public Value call(boolean w, BitArr x) {
      if (w) return BitArr.fill(x, true);
      return x;
    }
    @Override public Value call(BitArr w, boolean x) {
      if (x) return BitArr.fill(w, true);
      return w;
    }
    @Override public Value call(BitArr w, BitArr x) {
      BitArr.BC bc = new BitArr.BC(w.shape);
      for (int i = 0; i < w.arr.length; i++) bc.arr[i] = w.arr[i] | x.arr[i];
      return bc.finish();
    }
  };
  public Value call(Value w, Value x) {
    return bitD(DNF, DBF, w, x);
  }
}