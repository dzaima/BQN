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
      return v;
    }, x);
  }
  
  public static final D_NNeN DNF = new D_NNeN() {
    public double on(double w, double x) {
      return w + x;
    }
    public void on(double[] res, double w, double[] x) {
      for (int i = 0; i < x.length; i++) res[i] = w + x[i];
    }
    public void on(double[] res, double[] w, double x) {
      for (int i = 0; i < w.length; i++) res[i] = w[i] + x;
    }
    public void on(double[] res, double[] w, double[] x) {
      for (int i = 0; i < w.length; i++) res[i] = w[i] + x[i];
    }
    public Value call(BigValue w, BigValue x) {
      return new BigValue(w.i.add(x.i));
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