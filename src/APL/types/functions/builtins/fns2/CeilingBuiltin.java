package APL.types.functions.builtins.fns2;

import APL.types.*;
import APL.types.functions.Builtin;

public class CeilingBuiltin extends Builtin {
  @Override public String repr() {
    return "⌈";
  }
  
  
  
  public Value identity() {
    return Num.NEGINF;
  }
  
  private static final NumMV NF = new NumMV() {
    public Value call(Num x) {
      return x.ceil();
    }
    public void call(double[] res, double[] x) {
      for (int i = 0; i < x.length; i++) res[i] = Math.ceil(x[i]);
    }
  };
  public Value call(Value x) {
    return numChrM(NF, Char::upper, x);
  }
  
  private static final D_NNeN DNF = new D_NNeN() {
    public double on(double w, double x) {
      return Math.max(w, x);
    }
    public void on(double[] res, double w, double[] x) {
      for (int i = 0; i < x.length; i++) res[i] = Math.max(w, x[i]);
    }
    public void on(double[] res, double[] w, double x) {
      for (int i = 0; i < w.length; i++) res[i] = Math.max(w[i], x);
    }
    public void on(double[] res, double[] w, double[] x) {
      for (int i = 0; i < w.length; i++) res[i] = Math.max(w[i], x[i]);
    }
    public Value call(BigValue w, BigValue x) {
      return w.compareTo(x)>0? w : x;
    }
  };
  public Value call(Value w, Value x) {
    return numD(DNF, w, x);
  }
}