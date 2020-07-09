package APL.types.functions.builtins.fns2;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.functions.Builtin;

import java.math.BigInteger;

public class StarBuiltin extends Builtin {
  @Override public String repr() {
    return "⋆";
  }
  
  
  
  public static final NumMV NF = new NumMV() {
    public Value call(Num x) {
      return Num.E.pow(x);
    }
    public void call(double[] res, double[] x) {
      for (int i = 0; i < x.length; i++) res[i] = Math.exp(x[i]);
    }
  };
  public Value call(Value x) {
    return numM(NF, x);
  }
  public Value callInv(Value x) {
    return numM(LogBuiltin.NF, x);
  }
  
  public static final D_NNeN DNF = new D_NNeN() {
    public double on(double w, double x) {
      return Math.pow(w, x);
    }
    public void on(double[] res, double w, double[] x) {
      for (int i = 0; i < x.length; i++) res[i] = Math.pow(w, x[i]);
    }
    public void on(double[] res, double[] w, double x) {
      if (x == 2) for (int i = 0; i < w.length; i++) res[i] = w[i]*w[i];
      else for (int i = 0; i < w.length; i++) res[i] = Math.pow(w[i], x);
    }
    public void on(double[] res, double[] w, double[] x) {
      for (int i = 0; i < w.length; i++) res[i] = Math.pow(w[i], x[i]);
    }
    public Value call(BigValue w, BigValue x) {
      if (w.i.signum() == 0) return BigValue.ZERO;
      if (w.i.equals(BigInteger.ONE)) return BigValue.ONE;
      if (w.i.equals(BigValue.MINUS_ONE.i)) return x.i.intValue()%2 == 0? BigValue.ONE : BigValue.MINUS_ONE;
      if (x.i.bitLength() > 30) throw new DomainError("⋆: 𝕩 too big to calculate (𝕨 ≡ "+w+"; 𝕩 ≡ "+x+")", x); // otherwise intValue might ignore those!
      return new BigValue(w.i.pow(x.i.intValue()));
    }
  };
  public Value call(Value w, Value x) {
    return numD(DNF, w, x);
  }
  public Value callInvW(Value w, Value x) {
    return numD(LogBuiltin.DNF, w, x);
  }
  public Value callInvA(Value w, Value x) {
    return numD(RootBuiltin.DNF, x, w);
  }
}