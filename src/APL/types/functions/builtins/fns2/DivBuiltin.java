package APL.types.functions.builtins.fns2;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.functions.Builtin;

public class DivBuiltin extends Builtin {
  @Override public String repr() {
    return "÷";
  }
  
  
  
  private static final NumMV NF = new NumMV() {
    public Value call(Num x) {
      return new Num(1/x.num);
    }
    public void call(double[] res, double[] x) {
      for (int i = 0; i < x.length; i++) res[i] = 1/x[i];
    }
    public Value call(BigValue x) {
      throw new DomainError("reciprocal of biginteger", x);
    }
  };
  public Value call(Value x) {
    return numM(NF, x);
  }
  
  private static final D_NNeN DNF = new D_NNeN() {
    public double on(double w, double x) {
      return w/x;
    }
    public void on(double[] res, double w, double[] x) {
      for (int i = 0; i < x.length; i++) res[i] = w / x[i];
    }
    public void on(double[] res, double[] w, double x) {
      for (int i = 0; i < w.length; i++) res[i] = w[i] / x;
    }
    public void on(double[] res, double[] w, double[] x) {
      for (int i = 0; i < w.length; i++) res[i] = w[i] / x[i];
    }
    public Value call(BigValue w, BigValue x) {
      return new BigValue(w.i.divide(x.i));
    }
  };
  public Value call(Value w, Value x) {
    return numD(DNF, w, x);
  }
  
  public Value callInv(Value x) { return call(x); }
  public Value callInvW(Value w, Value x) { return call(w, x); }
  
  @Override public Value callInvA(Value w, Value x) {
    return numD(MulBuiltin.DNF, w, x);
  }
}