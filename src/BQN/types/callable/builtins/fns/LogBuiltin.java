package BQN.types.callable.builtins.fns;

import BQN.errors.DomainError;
import BQN.tools.*;
import BQN.types.*;
import BQN.types.callable.builtins.FnBuiltin;

public class LogBuiltin extends FnBuiltin { // here only to serve as DNF/NF for *⁼ & √˜⁼˜
  public String ln(FmtInfo f) { return "*⁼"; }
  
  static final double LN2 = Math.log(2);
  
  public static final NumMV NF = new NumMV() {
    public Value call(Num x) {
      return new Num(Math.log(x.num));
    }
    public void call(double[] res, double[] x) {
      for (int i = 0; i < x.length; i++) res[i] = Math.log(x[i]);
    }
    public Value call(BigValue x) {
      if (x.i.signum() <= 0) {
        if (x.i.signum() == -1) throw new DomainError("logarithm of negative number");
        return Num.NEGINF;
      }
      int len = x.i.bitLength();
      if (len<1023) return new Num(Math.log(x.i.doubleValue())); // safe quick path
      int shift = len-64; // 64 msb should be enough to get most out of log; safe quick path takes care of bitLength<64
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
  
  public static final Pervasion.NN2N DF = new Pervasion.NN2N() {
    public double on(double w, double x) { return Math.log(x) / Math.log(w); }
    public void on(double w, double[] x, double[] res) {
      double la = Math.log(w);
      for (int i = 0; i < x.length; i++) res[i] = Math.log(x[i]) / la;
    }
    public void on(double[] w, double x, double[] res) {
      double lw = Math.log(x);
      for (int i = 0; i < w.length; i++) res[i] = lw / Math.log(w[i]);
    }
    public void on(double[] w, double[] x, double[] res) {
      for (int i = 0; i < w.length; i++) res[i] = Math.log(x[i]) / Math.log(w[i]);
    }
    public Value on(Primitive w, Primitive x) {
      if (w instanceof Num && x instanceof BigValue) {
        double wd = ((Num) w).num;
        double res = ((Num) NF.call(((BigValue) x))).num/Math.log(wd);
        if (wd == 2) { // quick path to make sure 2⍟ makes sense
          int expected = ((BigValue) x).i.bitLength()-1;
          // System.out.println(res+" > "+expected);
          if (res < expected) return Num.of(expected);
          if (res >= expected+1) { // have to get the double juuuust below expected
            long repr = Double.doubleToRawLongBits(expected+1);
            repr--; // should be safe as positive int values are always well into the proper double domain
            return new Num(Double.longBitsToDouble(repr));
          }
        }
        return new Num(res);
      } else return super.on(w, x);
    }
  };
  public Value call(Value w, Value x) {
    return DF.call(w, x);
  }
  
  @Override public Value callInvX(Value w, Value x) {
    return StarBuiltin.DF.call(w, x);
  }
  @Override public Value callInvW(Value w, Value x) {
    return RootBuiltin.DF.call(w, x);
  }
}