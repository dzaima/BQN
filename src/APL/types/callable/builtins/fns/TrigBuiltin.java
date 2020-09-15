package APL.types.callable.builtins.fns;

import APL.errors.*;
import APL.tools.Pervasion;
import APL.types.*;
import APL.types.callable.builtins.FnBuiltin;

public class TrigBuiltin extends FnBuiltin {
  @Override public String repr() {
    return "○";
  }
  
  
  
  private static final NumMV NF = new NumMV() {
    public Value call(Num x) {
      return new Num(x.num * Math.PI);
    }
    public void call(double[] res, double[] x) {
      for (int i = 0; i < x.length; i++) res[i] = x[i] * Math.PI;
    }
  };
  private static final NumMV NFi = new NumMV() {
    public Value call(Num x) {
      return new Num(x.num / Math.PI);
    }
    public void call(double[] res, double[] x) {
      for (int i = 0; i < x.length; i++) res[i] = x[i] / Math.PI;
    }
  };
  
  public Value call(Value x) {
    return numM(NF, x);
  }
  public Value callInv(Value x) {
    return numM(NFi, x);
  }
  
  static final Pervasion.NN2NDef DF = new Pervasion.NN2NDef() {
    public double on(double w, double x) {
      switch((int) w) {
        case  1: return Math.sin(x);
        case  2: return Math.cos(x);
        case  3: return Math.tan(x);
        case  4: return Math.sqrt(x*x + 1);
        case  5: return Math.sinh(x);
        case  6: return Math.cosh(x);
        case  7: return Math.tanh(x);
        case  8: return Double.NaN; // pointless
        case  9: return x; // pointless
        case 10: return Math.abs(x); // pointless
        case 11: return 0; // also pointless
        case 12: throw new DomainError("what even is phase");
        
        case  0: return Math.sqrt(1 - x*x); //Num.ONE.minus(n.pow(Num.TWO)).root(Num.TWO);
        case  -1: return Math.asin(x);
        case  -2: return Math.acos(x);
        case  -3: return Math.atan(x);
        case  -4: return Math.sqrt(x*x - 1);
        case  -5: throw new NYIError("inverse hyperbolic functions"); // return Math.asinh(w);
        case  -6: throw new NYIError("inverse hyperbolic functions"); // return Math.acosh(w);
        case  -7: throw new NYIError("inverse hyperbolic functions"); // return Math.atanh(w);
        case  -8: return Double.NaN; // pooointleeeessssss
        case  -9: return x; // again, pointless pointless pointless
        case -10: return x;
        case -11: throw new DomainError("no complex numbers :/");
        case -12: throw new DomainError("no complex numbers no idea why this is even special-cased");
      }
      throw new DomainError("○: 𝕨 is out of bounds");
    }
  };
  public Value call(Value w, Value x) {
    return DF.call(w, x);
  }
  
  
  static final Pervasion.NN2NDef DFi = new Pervasion.NN2NDef() {
    @Override public double on(double w, double x) {
      switch((int) w) {
        case  1: return Math.asin(x);
        case  2: return Math.acos(x);
        case  3: return Math.atan(x);
        
        case -1: return Math.sin(x);
        case -2: return Math.cos(x);
        case -3: return Math.tan(x);
      }
      throw new DomainError("○⁼: 𝕩 must be in (+,-)1…3");
    }
  };
  public Value callInvX(Value w, Value x) {
    return DFi.call(w, x);
  }
}