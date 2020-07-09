package APL.types.functions.builtins.fns2;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.arrs.BitArr;
import APL.types.functions.Builtin;


public class GEBuiltin extends Builtin {
  @Override public String repr() {
    return "≥";
  }
  
  
  
  private static final D_NNeB DNF = new D_NNeB() {
    public boolean on(double w, double x) {
      return w >= x;
    }
    public void on(BitArr.BA res, double w, double[] x) {
      for (double cw : x) res.add(w >= cw);
    }
    public void on(BitArr.BA res, double[] w, double x) {
      for (double ca : w) res.add(ca >= x);
    }
    public void on(BitArr.BA res, double[] w, double[] x) {
      for (int i = 0; i < w.length; i++) res.add(w[i] >= x[i]);
    }
    public Value call(BigValue w, BigValue x) {
      return w.i.compareTo(x.i) >= 0? Num.ONE : Num.ZERO;
    }
  };
  
  public Value call(Value w0, Value x0) {
    return numChrD(DNF, (w, x) -> w>=x? Num.ONE : Num.ZERO,
      (w, x) -> { throw new DomainError("comparing "+ w.humanType(true)+" and "+x.humanType(true), this); },
      w0, x0
    );
  }
}