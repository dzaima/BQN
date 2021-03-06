package BQN.types.callable.builtins.fns;

import BQN.tools.*;
import BQN.types.*;
import BQN.types.callable.builtins.FnBuiltin;

public class RootBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "√"; }
  
  public static final NumMV NF = new NumMV() {
    public Value call(Num x) {
      return new Num(Math.sqrt(x.num));
    }
    public void call(double[] res, double[] x) {
      for (int i = 0; i < x.length; i++) res[i] = Math.sqrt(x[i]);
    }
  };
  public static final NumMV NFi = new NumMV() {
    public Value call(Num x) {
      return Num.of(x.num*x.num);
    }
    public void call(double[] res, double[] x) {
      for (int i = 0; i < x.length; i++) res[i] = x[i]*x[i];
    }
  };
  public Value call(Value x) {
    return numM(NF, x);
  }
  public Value callInv(Value x) {
    return numM(NFi, x);
  }
  
  public static final Pervasion.NN2N DF = new Pervasion.NN2N() {
    public double on(double w, double x) { return Math.pow(x, 1/w); }
    public void on(double   w, double[] x, double[] res) {
      double pow = 1/w;
      for (int i = 0; i < x.length; i++) res[i] = Math.pow(x[i], pow);
    }
    public void on(double[] w, double   x, double[] res) { for (int i = 0; i < w.length; i++) res[i] = Math.pow(x   , 1/w[i]); }
    public void on(double[] w, double[] x, double[] res) { for (int i = 0; i < w.length; i++) res[i] = Math.pow(x[i], 1/w[i]); }
  };
  public Value call(Value w, Value x) {
    return DF.call(w, x);
  }
  
  public Value callInvX(Value w, Value x) {
    return StarBuiltin.DF.call(x, w);
  }
  public Value callInvW(Value w, Value x) {
    return LogBuiltin.DF.call(w, x);
  }
}