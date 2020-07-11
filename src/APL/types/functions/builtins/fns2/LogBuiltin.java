package APL.types.functions.builtins.fns2;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.functions.Builtin;

public class LogBuiltin extends Builtin { // here only to serve as DNF/NF for *⁼ & √˜⁼˜
  @Override public String repr() {
    return "*⁼";
  }
  
  
  static final double LN2 = Math.log(2);
  
  public static final NumMV NF = new NumMV() {
    public Value call(Num x) {
      return new Num(Math.log(x.num));
    }
    public void call(double[] res, double[] x) {
      for (int i = 0; i < x.length; i++) res[i] = Math.log(x[i]);
    }
    public Num call(BigValue x) {
      if (x.i.signum() <= 0) {
        if (x.i.signum() == -1) throw new DomainError("logarithm of negative number", x);
        return Num.NEGINF;
      }
      if (x.i.bitLength()<1023) return new Num(Math.log(x.i.doubleValue())); // safe quick path
      int len = x.i.bitLength();
      int shift = len > 64? len - 64 : 0; // 64 msb should be enough to get most out of log
      double d = x.i.shiftRight(shift).doubleValue();
      return new Num(Math.log(d) + LN2*shift);
    }
  };
  public Value call(Value x) {
    return numM(NF, x);
  }
  public Value callInv(Value x) {
    return numM(StarBuiltin.NF, x);
  }
  
  public static final D_NNeN DNF = new D_NNeN() {
    public double on(double w, double x) {
      return Math.log(x) / Math.log(w);
    }
    public void on(double[] res, double w, double[] x) {
      double la = Math.log(w);
      for (int i = 0; i < x.length; i++) res[i] = Math.log(x[i]) / la;
    }
    public void on(double[] res, double[] w, double x) {
      double lw = Math.log(x);
      for (int i = 0; i < w.length; i++) res[i] = lw / Math.log(w[i]);
    }
    public void on(double[] res, double[] w, double[] x) {
      for (int i = 0; i < w.length; i++) res[i] = Math.log(x[i]) / Math.log(w[i]);
    }
    public Value call(double w, BigValue x) {
      double res = ((Num) NF.call(x)).num/Math.log(w);
      if (w==2) { // quick path to make sure 2⍟ makes sense
        int expected = x.i.bitLength()-1;
        // System.out.println(res+" > "+expected);
        if (res < expected) return Num.of(expected);
        if (res >= expected+1) { // have to get the double juuuust below expected
          long repr = Double.doubleToRawLongBits(expected+1);
          repr--; // should be safe as positive int values are always well into the proper double domain
          return new Num(Double.longBitsToDouble(repr));
        }
      }
      return new Num(res);
    }
  };
  public Value call(Value w, Value x) {
    return numD(DNF, w, x);
  }
  
  @Override public Value callInvW(Value w, Value x) {
    return numD(StarBuiltin.DNF, w, x);
  }
  @Override public Value callInvA(Value w, Value x) {
    return numD(RootBuiltin.DNF, w, x);
  }
}