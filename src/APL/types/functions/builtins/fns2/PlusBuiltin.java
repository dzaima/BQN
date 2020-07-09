package APL.types.functions.builtins.fns2;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.functions.Builtin;


public class PlusBuiltin extends Builtin {
  @Override public String repr() {
    return "+";
  }
  
  
  
  public Value identity() {
    return Num.ZERO;
  }
  
  public Value call(Value x) {
    return allM(v -> {
      if (!(v instanceof Num)) throw new DomainError("Conjugating a non-number", this, x); // TODO decide whether this should exist
      return ((Num)v).conjugate();
    }, x);
  }
  
  public static final D_NNeN DNF = new D_NNeN() {
    public double on(double a, double w) {
      return a + w;
    }
    public void on(double[] res, double a, double[] w) {
      for (int i = 0; i < w.length; i++) res[i] = a + w[i];
    }
    public void on(double[] res, double[] a, double w) {
      for (int i = 0; i < a.length; i++) res[i] = a[i] + w;
    }
    public void on(double[] res, double[] a, double[] w) {
      for (int i = 0; i < a.length; i++) res[i] = a[i] + w[i];
    }
    public Value call(BigValue a, BigValue w) {
      return new BigValue(a.i.add(w.i));
    }
  };
  public Value call(Value w, Value x) {
    return numD(DNF, w, x);
  }
  public Value callInv(Value x) { return call(x); }
  public Value callInvW(Value w, Value x) {
    return numD(MinusBuiltin.DNF, x, w);
  }
  
  @Override public Value callInvA(Value w, Value x) {
    return callInvW(x, w);
  }
}