package BQN.types.callable.builtins.fns;

import BQN.errors.*;
import BQN.tools.FmtInfo;
import BQN.types.*;
import BQN.types.arrs.*;
import BQN.types.callable.builtins.FnBuiltin;

import java.math.BigInteger;
import java.util.*;

public class DTackBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "⊤"; }
  
  public Value call(Value x) {
    return call(Num.NUMS[2], x);
  }
  
  public Value callInv(Value x) {
    return UTackBuiltin.on(Num.NUMS[2], x, this);
  }
  public Value callInvX(Value w, Value x) {
    return UTackBuiltin.on(w, x, this);
  }
  
  public Value call(Value w, Value x) {
    return on(w, x, this);
  }
  
  public static Value on(Value w, Value x, Callable blame) {
    if (!(w instanceof Primitive)) {
      if (x instanceof BigValue) {
        ArrayList<Value> res2 = new ArrayList<>();
        BigInteger c2 = ((BigValue) x).i;
        for (int i = 0; i < w.ia; i++) {
          Value v = w.get(w.ia-i-1);
          BigInteger[] dr = c2.divideAndRemainder(BigValue.bigint(v));
          res2.add(v instanceof Num? (Value)new Num(dr[1].intValue()) : (Value)new BigValue(dr[1]));
          c2 = dr[0];
        }
        Collections.reverse(res2);
        return HArr.create(res2);
      }
      int[] sh = new int[x.r() + w.r()];
      if (w.r() != 1) throw new NYIError(blame+": 𝕨 with rank≥2 not yet implemented", blame);
      
      System.arraycopy(w.shape, 0, sh, 0, w.r()); // ≡ for (int i = 0; i < a.rank; i++) sh[i] = a.shape[i];
      System.arraycopy(x.shape, 0, sh, w.r(), x.r()); // ≡ for (int i = 0; i < w.rank; i++) sh[i+a.rank] = w.shape[i];
      if (w.ia == 0) return new EmptyArr(sh, Num.ZERO);
      double[] c = x.asDoubleArrClone();
      double[] b = w.asDoubleArr();
      double[] res = new double[x.ia * w.ia];
      for (int i = 1; i < b.length; i++) if (b[i] == 0) throw new DomainError(blame+": 𝕨 contained a 0 as not the 1st element", blame);
      int last = b[0] == 0? 1 : 0;
      for (int i = b.length-1; i >= last; i--) {
        int off = x.ia*i;
        double cb = b[i];
        for (int j = 0; j < x.ia; j++) {
          res[off + j] = c[j] % cb;
          c[j] = Math.floor(c[j] / cb);
        }
      }
      if (b[0] == 0) {
        System.arraycopy(c, 0, res, 0, x.ia); // ≡ for (int j = 0; j < w.ia; j++) res[j] = c[j];
      }
      return new DoubleArr(res, sh);
    }
    if (!(x instanceof Num)) {
      if (x instanceof BigValue) {
        BigInteger base = BigValue.bigint(w);
        boolean bigBase = w instanceof BigValue;
        BigInteger wlr = ((BigValue) x).i;
        int sign = wlr.signum();
        BigInteger wl = wlr.abs();
        int ibase = BigValue.safeInt(base);
        if (ibase <= 1) {
          if (ibase==1 && sign!=0) throw new DomainError(blame+": 𝕨=1 and 𝕩≠0 isn't possible", blame);
          if (ibase < 0) throw new DomainError(blame+": 𝕨 < 0", blame);
        }
        if (sign==0) return EmptyArr.SHAPE0N;
        if (ibase == 2) {
          int len = wl.bitLength();
          if (bigBase) {
            Value[] res = new Value[len];
            if (sign==1) for (int i = 0; i < len; i++) res[len-i-1] = wl.testBit(i)? BigValue.      ONE : BigValue.ZERO;
            else         for (int i = 0; i < len; i++) res[len-i-1] = wl.testBit(i)? BigValue.MINUS_ONE : BigValue.ZERO;
            return new HArr(res);
          } else if (sign == 1) {
            BitArr.BA res = new BitArr.BA(Arr.vecsh(len),true);
            for (int i = 0; i < len; i++) res.add(wl.testBit(len-i-1));
            return res.finish();
          } else {
            double[] res = new double[len];
            for (int i = 0; i < len; i++) res[i] = wl.testBit(len-i-1)? -1 : 0;
            return new DoubleArr(res);
          }
        }
        // if (ibase <= Character.MAX_RADIX) { // utilize the actually optimized base conversion of BigInteger.toString
        //   String str = wl.toString(ibase);
        //   Value[] res = new Value[str.length()];
        //   for (int i = 0; i < res.length; i++) {
        //     char c = str.charAt(i);
        //     int n = c<='9'? c-'0' : 10+c-'a';
        //     if (sign==-1) n=-n;
        //     res[i] = bigBase? new BigValue(BigInteger.valueOf(n)) : Num.of(n);
        //   }
        //   return new HArr(res);
        // }
        ArrayList<Value> ns = new ArrayList<>(); // if we can't, just be lazy. ¯\_(ツ)_/¯
        while (wl.signum() != 0) {
          BigInteger[] c = wl.divideAndRemainder(base);
          wl = c[0];
          ns.add(bigBase? (Value)new BigValue(sign==1? c[1] : c[1].negate()) : new Num(c[1].intValue()*sign));
        }
        {
          Value[] res = new Value[ns.size()];
          for (int i = 0; i < res.length; i++) {
            res[res.length-i-1] = ns.get(i);
          }
          return new HArr(res);
        }
      }
      throw new NYIError(blame+": scalar 𝕨 and non-scalar 𝕩 not implemented", blame);
    }
    {
      double base = w.asDouble();
      double num = x.asDouble();
      if (base <= 1) {
        if (base == 0) return Num.of(num);
        if (base < 0) throw new DomainError(blame+": 𝕨 < 0", blame);
        throw new DomainError(blame+": 𝕨 < 1", blame);
      }
      ArrayList<Double> res = new ArrayList<>();
      if (num < 0) {
        num = -num;
        while (num > 0) {
          res.add(-num%base);
          num = Math.floor(num/base);
        }
      } else {
        while (num > 0) {
          res.add(num%base);
          num = Math.floor(num/base);
        }
      }
      double[] f = new double[res.size()];
      for (int i = res.size()-1, j = 0; i >= 0; i--, j++) {
        f[j] = res.get(i);
      }
      return new DoubleArr(f);
    }
  }
}