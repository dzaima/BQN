package APL.types.functions.builtins.fns;

import APL.types.*;
import APL.types.functions.Builtin;

public class DivBuiltin extends Builtin {
  public DivBuiltin() {
    super("÷", 0x011);
  }
  
  static class Nf implements NumMV {
    public Value call(Num w) {
      return Num.ONE.divide(w);
    }
    public void call(double[] res, double[] a) {
      for (int i = 0; i < a.length; i++) res[i] = 1/a[i];
    }
  }
  private static final Nf NF = new Nf();
  public Obj call(Value w) {
    return numM(NF, w);
  }
  static class DNf implements NumDV {
    public double call(double a, double w) {
      return a / w;
    }
    public void call(double[] res, double a, double[] w) {
      for (int i = 0; i < w.length; i++) res[i] = a / w[i];
    }
    public void call(double[] res, double[] a, double w) {
      for (int i = 0; i < a.length; i++) res[i] = a[i] / w;
    }
    public void call(double[] res, double[] a, double[] w) {
      for (int i = 0; i < a.length; i++) res[i] = a[i] / w[i];
    }
  }
  private static final DNf DNF = new DNf();
  public Obj call(Value a0, Value w0) {
    return numD(DNF, a0, w0);
  }
  
  public Obj callInv(Value w) { return call(w); }
  public Obj callInvW(Value a, Value w) { return call(a, w); }
}
