package APL.types.functions.builtins.fns2;

import APL.types.*;
import APL.types.functions.Builtin;

public class RootBuiltin extends Builtin {
  @Override public String repr() {
    return "√";
  }
  
  
  
  private static final NumMV NF = new NumMV() {
    public Value call(Num x) {
      return x.root(Num.NUMS[2]);
    }
    public void call(double[] res, double[] x) {
      for (int i = 0; i < x.length; i++) res[i] = Math.sqrt(x[i]);
    }
  };
  private static final NumMV NFi = new NumMV() {
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
  
  public static final D_NNeN DNF = new D_NNeN() {
    public double on(double w, double x) {
      return Math.pow(x, 1/w);
    }
    public void on(double[] res, double w, double[] x) {
      double pow = 1/w;
      for (int i = 0; i < x.length; i++) res[i] = Math.pow(x[i], pow);
    }
    public void on(double[] res, double[] w, double x) {
      for (int i = 0; i < w.length; i++) res[i] = Math.pow(x, 1/w[i]);
    }
    public void on(double[] res, double[] w, double[] x) {
      for (int i = 0; i < w.length; i++) res[i] = Math.pow(x[i], 1/w[i]);
    }
  };
  public Value call(Value w, Value x) {
    return numD(DNF, w, x);
  }
  
  public Value callInvW(Value w, Value x) {
    return numD(StarBuiltin.DNF, x, w);
  }
  public Value callInvA(Value w, Value x) {
    return numD(LogBuiltin.DNF, w, x);
  }
}