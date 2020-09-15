package APL.types.callable.builtins.fns;

import APL.errors.DomainError;
import APL.tools.Pervasion;
import APL.types.*;
import APL.types.callable.builtins.FnBuiltin;

import java.math.BigInteger;

public class ExclBuiltin extends FnBuiltin {
  @Override public String repr() {
    return "!";
  }
  
  
  private static final double[] cache = new double[172];
  static {
    double r = 1;
    cache[0] = cache[1] = r;
    for (int i = 2; i < 172; i++) {
      r*= i;
      cache[i] = r;
    }
  }
  
  private static final NumMV NF = new NumMV() {
    public Value call(Num x) {
      return new Num(cache[Math.min(x.asInt(), 171)]);
    }
    public void call(double[] res, double[] x) {
      for (int i = 0; i < x.length; i++) {
        res[i] = cache[Math.min((int) x[i], 171)];
      }
    }
    public Value call(BigValue x) {
      if (x.i.bitLength() > 30) throw new DomainError("!: argument too big (𝕩 ≡ "+x+")", x); // otherwise intValue might ignore those!
      int am = x.i.intValue();
      BigInteger res = BigInteger.ONE;
      for (int i = 2; i <= am; i++) {
        res = res.multiply(BigInteger.valueOf(i));
      }
      return new BigValue(res);
    }
  };
  
  public Value call(Value x) {
    return numM(NF, x);
  }
  
  
  static Pervasion.NN2NDef DF = new Pervasion.NN2NDef() {
    public Value on(BigValue w, BigValue x) {
      BigInteger res = BigInteger.ONE;
      BigInteger al = BigValue.bigint(x);
      BigInteger bl = BigValue.bigint(w);
      if (al.compareTo(bl) < 0) return Num.ZERO;
  
      if (bl.compareTo(al.subtract(bl)) > 0) bl = al.subtract(bl);
  
      if (bl.bitLength() > 30) throw new DomainError("!: arguments too big (𝕨 ≡ "+w+"; 𝕩 ≡ "+x+")", x);
      int ri = bl.intValue();
  
      for (int i = 0; i < ri; i++) {
        res = res.multiply(al.subtract(BigInteger.valueOf(i)));
      }
      for (int i = 0; i < ri; i++) {
        res = res.divide(BigInteger.valueOf(i+1));
      }
      return new BigValue(res);
    }
  
    public double on(double w, double x) {
      return binomial(w, x);
    }
  };
  
  public Value call(Value w, Value x) {
    return DF.call(w, x);
  }
  
  
  public static double binomial(double w, double x) {
    if (x % 1 != 0) throw new DomainError("binomial of non-integer 𝕨");
    if (w % 1 != 0) throw new DomainError("binomial of non-integer 𝕩");
    if (w > x) return 0;
    
    double res = 1;
    double a = x;
    double b = w;
    
    if (b > a-b) b = a-b;
    
    for (int i = 0; i < b; i++) {
      res = res*(a-i) / (i+1);
    }
    return res;
  }
}