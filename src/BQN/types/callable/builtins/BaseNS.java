package BQN.types.callable.builtins;

import BQN.errors.*;
import BQN.tools.FmtInfo;
import BQN.types.*;
import BQN.types.arrs.*;
import BQN.types.mut.SimpleMap;

import java.math.BigInteger;
import java.util.*;

public class BaseNS extends SimpleMap {
  public String ln(FmtInfo f) { return "•b"; }
  public static final Value INSTANCE = new BaseNS();
  
  
  // public static Value pack(Value w, Value x, Callable blame) {
  //   if (x.r() == 0) throw new DomainError("packing scalar is pointless", blame);
  //   if (w instanceof BigValue || w.first() instanceof BigValue || x.first() instanceof BigValue) {
  //     if (w.r() == 0) {
  //       BigInteger al = BigValue.bigint(w);
  //       BigInteger res = BigInteger.ZERO;
  //       for (int i = 0; i < x.ia; i++) res = res.multiply(al).add(BigValue.bigint(x.get(i)));
  //       return new BigValue(res);
  //     } else {
  //       if (x.r() != 1) throw new NYIError(blame+": 1 < =𝕩", blame);
  //       if (w.r() != 1) throw new DomainError(blame+": 1 < =𝕨", blame);
  //       if (w.ia != x.shape[0]) throw new DomainError(blame+": (≠𝕨) ≠ ≠𝕩", blame);
  //       BigInteger res = BigInteger.ZERO;
  //       for (int i = 0; i < w.ia; i++) {
  //         res = res.multiply(BigValue.bigint(w.get(i)));
  //         res = res.add(BigValue.bigint(x.get(i)));
  //       }
  //       return new BigValue(res);
  //     }
  //   }
  //   if (w instanceof Num) {
  //     double base = w.asDouble();
  //     if (x.r() == 1) {
  //       double res = 0;
  //       for (int i = 0; i < x.ia; i++) res = res*base + x.get(i).asDouble();
  //       return new Num(res);
  //     } else {
  //       double[] d = x.asDoubleArr();
  //       int[] sh = new int[x.r()-1];
  //       System.arraycopy(x.shape, 1, sh, 0, x.r() - 1);
  //       int layers = x.shape[0];
  //       double[] r = new double[x.ia / layers];
        
  //       System.arraycopy(d, 0, r, 0, r.length);
  //       for (int i = 1; i < layers; i++) {
  //         for (int j = 0; j < r.length; j++) {
  //           r[j] = r[j]*base + d[j+r.length*i];
  //         }
  //       }
        
  //       return new DoubleArr(r, sh);
  //     }
  //   } else {
  //     if (w.ia != x.shape[0]) throw new DomainError(blame+": (≠𝕨) ≠ ⊑≢𝕩", blame);
  //     double[] d = x.asDoubleArr();
  //     double[] bases = w.asDoubleArr();
  //     int[] sh = new int[x.r()-1];
  //     System.arraycopy(x.shape, 1, sh, 0, x.r() - 1);
  //     int layers = x.shape[0];
  //     double[] r = new double[x.ia/layers];
      
  //     System.arraycopy(d, 0, r, 0, r.length);
  //     for (int i = 1; i < layers; i++) {
  //       double base = bases[i];
  //       for (int j = 0; j < r.length; j++) {
  //         r[j] = r[j]*base + d[j+r.length*i];
  //       }
  //     }
  //     if (sh.length == 0) return new Num(r[0]);
  //     return new DoubleArr(r, sh);
  //   }
  // }
  
  // public static Value unpack(Value a, Value w, Callable blame) {
  //   if (!(a instanceof Primitive)) {
  //     if (w instanceof BigValue) {
  //       ArrayList<Value> res = new ArrayList<>();
  //       BigInteger c = ((BigValue) w).i;
  //       for (int i = 0; i < a.ia; i++) {
  //         Value v = a.get(a.ia-i-1);
  //         BigInteger[] dr = c.divideAndRemainder(BigValue.bigint(v));
  //         res.add(v instanceof Num? new Num(dr[1].intValue()) : new BigValue(dr[1]));
  //         c = dr[0];
  //       }
  //       Collections.reverse(res);
  //       return Arr.create(res);
  //     } else {
  //       int[] sh = new int[w.r()+a.r()];
  //       if (a.r() != 1) throw new NYIError(blame+": 2≤=𝕨", blame);
        
  //       System.arraycopy(a.shape, 0, sh, 0, a.r()); // ≡ for (int i = 0; i < a.r(); i++) sh[i] = a.shape[i];
  //       System.arraycopy(w.shape, 0, sh, a.r(), w.r()); // ≡ for (int i = 0; i < w.r(); i++) sh[i+a.r()] = w.shape[i];
  //       if (a.ia == 0) return new EmptyArr(sh, Num.ZERO);
  //       double[] c = w.asDoubleArrClone();
  //       double[] b = a.asDoubleArr();
  //       double[] res = new double[w.ia * a.ia];
  //       for (int i = 1; i < b.length; i++) if (b[i] == 0) throw new DomainError(blame+": 𝕨 contained a 0 as not the 1st element", blame);
  //       int last = b[0] == 0? 1 : 0;
  //       for (int i = b.length-1; i >= last; i--) {
  //         int off = w.ia*i;
  //         double cb = b[i];
  //         for (int j = 0; j < w.ia; j++) {
  //           res[off + j] = c[j] % cb;
  //           c[j] = Math.floor(c[j] / cb);
  //         }
  //       }
  //       if (b[0] == 0) {
  //         System.arraycopy(c, 0, res, 0, w.ia); // ≡ for (int j = 0; j < w.ia; j++) res[j] = c[j];
  //       }
  //       return new DoubleArr(res, sh);
  //     }
  //   }
  //   if (!(w instanceof Num)) {
  //     if (w instanceof BigValue) {
  //       BigInteger base = BigValue.bigint(a);
  //       boolean bigBase = a instanceof BigValue;
  //       BigInteger wlr = ((BigValue) w).i;
  //       int sign = wlr.signum();
  //       BigInteger wl = wlr.abs();
  //       int ibase = BigValue.safeInt(base);
  //       if (ibase <= 1) {
  //         if (ibase==1 && sign!=0) throw new DomainError(blame+": 𝕨=1 and 𝕩≠0 isn't possible", blame);
  //         if (ibase < 0) throw new DomainError(blame+": 𝕨 < 0", blame);
  //       }
  //       if (sign==0) return EmptyArr.SHAPE0N;
  //       if (ibase == 2) {
  //         int len = wl.bitLength();
  //         if (bigBase) {
  //           Value[] res = new Value[len];
  //           if (sign==1) for (int i = 0; i < len; i++) res[len-i-1] = wl.testBit(i)? BigValue.      ONE : BigValue.ZERO;
  //           else         for (int i = 0; i < len; i++) res[len-i-1] = wl.testBit(i)? BigValue.MINUS_ONE : BigValue.ZERO;
  //           return new HArr(res);
  //         } else if (sign == 1) {
  //           BitArr.BA bc = new BitArr.BA(Arr.vecsh(len), true);
  //           for (int i = 0; i < len; i++) bc.add(wl.testBit(len-i-1));
  //           return bc.finish();
  //         } else {
  //           double[] res = new double[len];
  //           for (int i = 0; i < len; i++) res[i] = wl.testBit(len-i-1)? -1 : 0;
  //           return new DoubleArr(res);
  //         }
  //       } else {
  //         // if (ibase <= Character.MAX_RADIX) { // utilize the actually optimized base conversion of BigInteger.toString
  //         //   String str = wl.toString(ibase);
  //         //   Value[] res = new Value[str.length()];
  //         //   for (int i = 0; i < res.length; i++) {
  //         //     char c = str.charAt(i);
  //         //     int n = c<='9'? c-'0' : 10+c-'a';
  //         //     if (sign==-1) n=-n;
  //         //     res[i] = bigBase? new BigValue(BigInteger.valueOf(n)) : Num.of(n);
  //         //   }
  //         //   return new HArr(res);
  //         // } else {
  //         ArrayList<Value> ns = new ArrayList<>(); // if we can't, just be lazy. ¯\_(ツ)_/¯
  //         while (wl.signum() != 0) {
  //           BigInteger[] c = wl.divideAndRemainder(base);
  //           wl = c[0];
  //           ns.add(bigBase? new BigValue(sign==1? c[1] : c[1].negate()) : new Num(c[1].intValue()*sign));
  //         }
  //         Value[] res = new Value[ns.size()];
  //         for (int i = 0; i < res.length; i++) {
  //           res[res.length-i-1] = ns.get(i);
  //         }
  //         return new HArr(res);
  //         // }
  //       }
  //     }
  //     throw new NYIError(blame+": scalar 𝕨 and non-scalar 𝕩 not implemented", blame);
  //   } else {
  //     double base = a.asDouble();
  //     double num = w.asDouble();
  //     if (base <= 1) {
  //       if (base == 0) return Num.of(num);
  //       if (base < 0) throw new DomainError(blame+": 𝕨 < 0", blame);
  //       throw new DomainError(blame+": 𝕨 < 1", blame);
  //     }
  //     ArrayList<Double> res = new ArrayList<>();
  //     if (num < 0) {
  //       num = -num;
  //       while (num > 0) {
  //         res.add(-num%base);
  //         num = Math.floor(num/base);
  //       }
  //     } else {
  //       while (num > 0) {
  //         res.add(num%base);
  //         num = Math.floor(num/base);
  //       }
  //     }
  //     double[] f = new double[res.size()];
  //     for (int i = res.size()-1, j = 0; i >= 0; i--, j++) {
  //       f[j] = res.get(i);
  //     }
  //     return new DoubleArr(f);
  //   }
  // }
  
  // public static Value format(Value w, Value x) {
  //   int base;
  //   int len;
  //   if (w.ia == 2) {
  //     base = w.get(0).asInt();
  //     len = w.get(1).asInt();
  //   } else {
  //     base = w.asInt();
  //     len = 0;
  //   }
  //   long val = (long)x.asDouble();
  //   String s = Long.toString(val, base).toUpperCase();
  //   while (s.length()<len) s = "0"+s;
  //   return new ChrArr(s);
  // }
  // public static Value read(Value w, Value x) {
  //   int base = w.asInt();
  //   String s = x.asString();
  //   return new Num(Long.parseLong(s, base));
  // }
  // public static Value toDigits(Value x) {
  //   String s = x.asString();
  //   int[] r = new int[x.ia];
  //   for (int i = 0; i < r.length; i++) {
  //     char c = s.charAt(i);
  //     r[i] = c>='0'&c<='9'? c-'0' : c>='A'&c<='Z'? c-'A'+10 : c>='a'&c<='z'? c-'a'+10 : -1;
  //     if (r[i]==-1) throw new DomainError("Bad base character '"+c+"'");
  //   }
  //   return new IntArr(r, x.shape);
  // }
  // public static Value toChars(Value x) {
  //   int[] xi = x.asIntArr();
  //   char[] r = new char[x.ia];
  //   for (int i = 0; i < r.length; i++) {
  //     if (xi[i]<0 | xi[i]>=36) throw new DomainError("Can't •b.format number "+xi[i]);
  //     r[i] = (char) (xi[i]<10? '0' + xi[i] : 'A' + xi[i]-10);
  //   }
  //   return new ChrArr(r, x.shape);
  // }
  
  // public static final BB p = new BB("Pack") {
  //   public Value call(Value x         ) { return pack(Num.NUMS[2], x, this); }
  //   public Value call(Value w, Value x) { return pack(w          , x, this); }
  //   public Value callInv (         Value x) { return unpack(Num.NUMS[2], x, this); }
  //   public Value callInvX(Value w, Value x) { return unpack(w          , x, this); }
  // };
  // public static final BB u = new BB("Unpack") {
  //   public Value call(Value x         ) { return unpack(Num.NUMS[2], x, this); }
  //   public Value call(Value w, Value x) { return unpack(w          , x, this); }
  //   public Value callInv (         Value x) { return pack(Num.NUMS[2], x, this); }
  //   public Value callInvX(Value w, Value x) { return pack(w          , x, this); }
  // };
  // public static final BB f = new BB("Format") {
  //   public Value call   (Value x) { return toChars(x); }
  //   public Value callInv(Value x) { return toDigits(x); }
  //   public Value call    (Value w, Value x) { return format(w, x); }
  //   public Value callInvX(Value w, Value x) { return read  (w, x); }
  // };
  // public static final BB rd = new BB("Read") {
  //   public Value call   (Value x) { return toDigits(x); }
  //   public Value callInv(Value x) { return toChars(x); }
  //   public Value call    (Value w, Value x) { return read  (w, x); }
  //   public Value callInvX(Value w, Value x) { return format(w, x); }
  // };
  
  public Value getv(String s) {
    // switch (s) {
    //   case "p": case "pack": return p;
    //   case "u": case "unpack": return u;
    //   case "f": case "format": return f;
    //   case "r": case "read": case "parse": return rd;
    // }
    throw new ValueError("No key "+s+" in •b");
  }
  
  public void setv(String s, Value v) {
    throw new DomainError("Assigning into •b");
  }
  
  public static class BB extends FnBuiltin {
    public final String name;
    public BB(String name) {
      this.name = "•b."+name;
    }
    
    public String ln(FmtInfo f) { return name; }
  }
}
