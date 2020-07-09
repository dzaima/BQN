package APL.types.functions.builtins.fns;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.functions.Builtin;

import java.math.BigInteger;

public class ExclBuiltin extends Builtin {
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
  
  public Value call(Value w0, Value x0) {
    return allD((w, x) -> {
      if (w instanceof BigValue || x instanceof BigValue) {
        
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
      return ((Num) x).binomial((Num) w);
    }, w0, x0);
  }
}