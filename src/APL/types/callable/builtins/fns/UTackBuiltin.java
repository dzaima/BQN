package APL.types.callable.builtins.fns;

import APL.errors.*;
import APL.tools.FmtInfo;
import APL.types.*;
import APL.types.arrs.DoubleArr;
import APL.types.callable.builtins.FnBuiltin;

import java.math.BigInteger;

public class UTackBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "⊥"; }
  
  public Value call(Value x) {
    return call(Num.NUMS[2], x);
  }
  
  public Value callInv(Value x) {
    return DTackBuiltin.on(Num.NUMS[2], x, this);
  }
  public Value callInvX(Value w, Value x) {
    return DTackBuiltin.on(w, x, this);
  }
  
  public Value call(Value w, Value x) {
    return on(w, x, this);
  }
  
  public static Value on(Value w, Value x, Callable blame) {
    if (x.r() == 0) throw new DomainError("A⊥num is pointless", blame);
    if (w instanceof BigValue || w.first() instanceof BigValue || x.first() instanceof BigValue) {
      if (w.r() == 0) {
        BigInteger al = BigValue.bigint(w);
        BigInteger res = BigInteger.ZERO;
        for (int i = 0; i < x.ia; i++) {
          res = res.multiply(al).add(BigValue.bigint(x.get(i)));
        }
        return new BigValue(res);
      } else {
        if (x.r() != 1) throw new NYIError(blame+": 1<≠≢𝕩", blame);
        if (w.r() != 1) throw new DomainError(blame+": 1<≠≢𝕨", blame);
        if (w.ia != x.shape[0]) throw new DomainError(blame+": (≠𝕨) ≠ ≠𝕩", blame);
        BigInteger res = BigInteger.ZERO;
        for (int i = 0; i < w.ia; i++) {
          res = res.multiply(BigValue.bigint(w.get(i)));
          res = res.add(BigValue.bigint(x.get(i)));
        }
        return new BigValue(res);
      }
    }
    if (w instanceof Num) {
      double base = w.asDouble();
      if (x.r() == 1) {
        double res = 0;
        for (int i = 0; i < x.ia; i++) {
          res = res*base + x.get(i).asDouble();
        }
        return new Num(res);
      } else {
        double[] d = x.asDoubleArr();
        int[] sh = new int[x.r()-1];
        System.arraycopy(x.shape, 1, sh, 0, x.r() - 1);
        int layers = x.shape[0];
        double[] r = new double[x.ia / layers];
        
        System.arraycopy(d, 0, r, 0, r.length);
        for (int i = 1; i < layers; i++) {
          for (int j = 0; j < r.length; j++) {
            r[j] = r[j]*base + d[j+r.length*i];
          }
        }
        
        return new DoubleArr(r, sh);
      }
    } else {
      if (w.ia != x.shape[0]) throw new DomainError(blame+": (≠𝕨) ≠ ≠𝕩", blame);
      double[] d = x.asDoubleArr();
      double[] bases = w.asDoubleArr();
      int[] sh = new int[x.r()-1];
      System.arraycopy(x.shape, 1, sh, 0, x.r() - 1);
      int layers = x.shape[0];
      double[] r = new double[x.ia/layers];
      
      System.arraycopy(d, 0, r, 0, r.length);
      for (int i = 1; i < layers; i++) {
        double base = bases[i];
        for (int j = 0; j < r.length; j++) {
          r[j] = r[j]*base + d[j+r.length*i];
        }
      }
      if (sh.length == 0) return new Num(r[0]);
      return new DoubleArr(r, sh);
    }
  }
}