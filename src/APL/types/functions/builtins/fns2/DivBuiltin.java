package APL.types.functions.builtins.fns2;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.functions.Builtin;

public class DivBuiltin extends Builtin {
  @Override public String repr() {
    return "÷";
  }
  
  
  
  private static final NumMV NF = new NumMV() {
    public Value call(Num w) {
      return Num.ONE.divide(w);
    }
    public void call(double[] res, double[] a) {
      for (int i = 0; i < a.length; i++) res[i] = 1/a[i];
    }
    public Value call(BigValue w) {
      throw new DomainError("reciprocal of biginteger", w);
    }
  };
  public Value call(Value x) {
    return numM(NF, x);
  }
  
  private static final D_NNeN DNF = new D_NNeN() {
    public double on(double a, double w) {
      return a / w;
    }
    public void on(double[] res, double a, double[] w) {
      for (int i = 0; i < w.length; i++) res[i] = a / w[i];
    }
    public void on(double[] res, double[] a, double w) {
      for (int i = 0; i < a.length; i++) res[i] = a[i] / w;
    }
    public void on(double[] res, double[] a, double[] w) {
      for (int i = 0; i < a.length; i++) res[i] = a[i] / w[i];
    }
    public Value call(BigValue a, BigValue w) {
      return new BigValue(a.i.divide(w.i));
    }
  };
  public Value call(Value w, Value x) {
    return numD(DNF, w, x);
  }
  
  public Value callInv(Value x) { return call(x); }
  public Value callInvW(Value a, Value w) { return call(a, w); }
  
  @Override public Value callInvA(Value a, Value w) {
    return numD(MulBuiltin.DNF, a, w);
  }
}