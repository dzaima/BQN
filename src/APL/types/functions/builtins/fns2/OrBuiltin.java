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
    public double on(double a, double w) {
      return a+w - a*w;
    }
    public void on(double[] res, double a, double[] w) {
      for (int i = 0; i < w.length; i++) res[i] = a+w[i] - a*w[i];
    }
    public void on(double[] res, double[] a, double w) {
      for (int i = 0; i < a.length; i++) res[i] = a[i]+w - a[i]*w;
    }
    public void on(double[] res, double[] a, double[] w) {
      for (int i = 0; i < a.length; i++) res[i] = a[i]+w[i] - a[i]*w[i];
    }
    public Value call(BigValue a, BigValue w) {
      return new BigValue(a.i.gcd(w.i));
    }
  };
  
  private static final D_BB DBF = new D_BB() {
    @Override public Value call(boolean a, BitArr w) {
      if (a) return BitArr.fill(w, true);
      return w;
    }
    @Override public Value call(BitArr a, boolean w) {
      if (w) return BitArr.fill(a, true);
      return a;
    }
    @Override public Value call(BitArr a, BitArr w) {
      BitArr.BC bc = new BitArr.BC(a.shape);
      for (int i = 0; i < a.arr.length; i++) bc.arr[i] = a.arr[i] | w.arr[i];
      return bc.finish();
    }
  };
  public Value call(Value a0, Value w0) {
    return bitD(DNF, DBF, a0, w0);
  }
}