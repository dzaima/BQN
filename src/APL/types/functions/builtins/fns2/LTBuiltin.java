package APL.types.functions.builtins.fns2;

import APL.Main;
import APL.errors.DomainError;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;


public class LTBuiltin extends Builtin {
  @Override public String repr() {
    return "<";
  }
  
  
  
  private static final D_NNeB DNF = new D_NNeB() {
    public boolean on(double a, double w) {
      return a < w;
    }
    public void on(BitArr.BA res, double a, double[] w) {
      for (double cw : w) res.add(a < cw);
    }
    public void on(BitArr.BA res, double[] a, double w) {
      for (double ca : a) res.add(ca < w);
    }
    public void on(BitArr.BA res, double[] a, double[] w) {
      for (int i = 0; i < a.length; i++) res.add(a[i] < w[i]);
    }
    public Value call(BigValue a, BigValue w) {
      return a.i.compareTo(w.i) < 0? Num.ONE : Num.ZERO;
    }
  };
  
  public Value call(Value w, Value x) {
    return numChrD(DNF, (ca, cw) -> ca<cw? Num.ONE : Num.ZERO,
      (ca, cw) -> { throw new DomainError("comparing "+ ca.humanType(true)+" and "+cw.humanType(true), this); },
      w, x
    );
  }
  
  public Value call(Value x) {
    if (!Main.enclosePrimitives && x instanceof Primitive) return x;
    return new Rank0Arr(x);
  }
}