package BQN.types.callable.builtins.md1;

import BQN.Main;
import BQN.errors.DomainError;
import BQN.tools.*;
import BQN.types.*;
import BQN.types.arrs.*;
import BQN.types.callable.Md1Derv;
import BQN.types.callable.builtins.Md1Builtin;
import BQN.types.callable.builtins.fns.*;

import java.util.Arrays;

public class ScanBuiltin extends Md1Builtin {
  public String ln(FmtInfo f) { return "`"; }
  
  public Value call(Value f, Value x, Md1Derv derv) {
    if (x.ia == 0) return x;
    if (x.r() == 0) throw new DomainError("`: rank must be at least 1, argument was a scalar", this);
    int l = Arr.prod(x.shape, 1, x.r());
    if (x.quickDoubleArr()) {
      Pervasion.NN2N fd = f.dyNum();
      if (fd != null) {
        double[] dres;
        int i = l;
        if (x.quickIntArr()) {
          if (x.r()==1 && x instanceof BitArr) {
            if (f instanceof NEBuiltin) {
              long[] xl = ((BitArr) x).arr;
              long[] res = new long[xl.length];
              long xor = 0;
              for (int j = 0; j < xl.length; j++) {
                long c = xl[j];
                long r = c ^ (c<<1);
                r^= r<< 2; r^= r<< 4; r^= r<<8;
                r^= r<<16; r^= r<<32; r^=  xor;
                res[j] = r;
                xor = r>>63; // copies sign bit
              }
              return new BitArr(res, x.shape);
            }
            if (f instanceof OrBuiltin) {
              long[] xl = ((BitArr) x).arr;
              for (int j = 0; j < xl.length; j++) {
                long c = xl[j];
                if (c!=0) {
                  long[] res = new long[xl.length];
                  int sh = Long.numberOfTrailingZeros(c);
                  res[j++] = -(1L << (sh));
                  while (j < xl.length) res[j++] = ~0L;
                  return new BitArr(res, x.shape);
                }
              }
              return BitArr.s0(x);
            }
            if (f instanceof PlusBuiltin) {
              int[] res = new int[x.ia];
              BitArr.BR xr = ((BitArr) x).read();
              int sum = 0;
              for (int j = 0; j < x.ia; j++) {
                sum+= xr.read()? 1 : 0;
                res[j] = sum;
              }
              return new IntArr(res, x.shape);
            }
          }
          int[] res3 = new int[x.ia];
          int[] xi = x.asIntArr();
          System.arraycopy(xi, 0, res3, 0, l);
          while (i < x.ia) {
            double n = fd.on(res3[i-l], xi[i]);
            if ((int)n != n) {
              dres = new double[x.ia]; for (int j = 0; j < dres.length; j++) dres[j] = res3[j];
              dres[i++] = n;
              goto IA_END;
            }
            res3[i++] = (int) n;
          }
          return new IntArr(res3, x.shape);
        } else dres = new double[x.ia];
        IA_END:
        double[] xd = x.asDoubleArr();
        System.arraycopy(xd, 0, dres, 0, l);
        while (i < x.ia) {
          dres[i] = fd.on(dres[i-l], xd[i]);
          i++;
        }
        return new DoubleArr(dres, x.shape);
      }
    }
    Value[] res2 = new Value[x.ia];
    int i2 = 0;
    for (; i2 < l; i2++) res2[i2] = x.get(i2);
    for (; i2 < x.ia; i2++) res2[i2] = f.call(res2[i2-l], x.get(i2));
    return Arr.create(res2, x.shape);
  }
  
  public Value callInv(Value f, Value x) {
    if (x.r()==0) throw new DomainError("F`⁼: argument had rank 0");
    if (x.ia==0) return x;
    Value[] res = new Value[x.ia];
    int l = Arr.prod(x.shape, 1, x.r());
    int i = 0;
    for (; i < l; i++) res[i] = x.get(i);
    for (; i < x.ia; i++) res[i] = f.callInvX(x.get(i-l), x.get(i));
    return Arr.create(res, x.shape);
  }
  
  
  
  
  public Value call(Value f, Value w, Value x, Md1Derv derv) {
    if (w.r()+1!=x.r() || !Arrays.equals(w.shape, Arrays.copyOfRange(x.shape, 1, x.r()))) throw new DomainError("`: 𝕨 must have shape 1↓≢𝕩", this);
    if (x.ia==0) return x;
    int l = w.ia;
    int i;
    if (w.quickDoubleArr() && x.quickDoubleArr()) {
      Pervasion.NN2N fd = f.dyNum();
      if (fd != null) {
        double[] dres;
        if (x.quickIntArr() && w.quickIntArr()) {
          if (x.r()==1 && x instanceof BitArr && Main.isBool(w)) {
            if (f instanceof NEBuiltin) {
              long[] xl = ((BitArr) x).arr;
              long[] res = new long[xl.length];
              long xor = ((Num) w).num==1?-1L:0;
              for (int j = 0; j < xl.length; j++) {
                long c = xl[j];
                long r = c ^ (c<<1);
                r^= r<< 2; r^= r<< 4; r^= r<<8;
                r^= r<<16; r^= r<<32; r^=  xor;
                res[j] = r;
                xor = r>>63; // copies sign bit
              }
              return new BitArr(res, x.shape);
            }
            if (f instanceof PlusBuiltin) {
              int[] res = new int[x.ia];
              BitArr.BR xr = ((BitArr) x).read();
              int sum = ((Num) w).num==0?0:1;
              for (int j = 0; j < x.ia; j++) {
                sum+= xr.read()? 1 : 0;
                res[j] = sum;
              }
              return new IntArr(res, x.shape);
            }
          }
          double tmp;
          int[] res2 = new int[x.ia];
          spec: {
            int[] xi = x.asIntArr();
            int[] wi = w.asIntArr();
            i = 0;
            while (i < l) {
              tmp = fd.on(wi[i], xi[i]);
              if ((int)tmp != tmp) break spec;
              res2[i++] = (int) tmp;
            }
            while (i < x.ia) {
              tmp = fd.on(res2[i-l], xi[i]);
              if ((int)tmp != tmp) break spec;
              res2[i++] = (int) tmp;
            }
            return new IntArr(res2, x.shape);
          }
          dres = new double[x.ia];
          for (int j = 0; j < i; j++) dres[j] = res2[j];
          dres[i++] = tmp;
          goto IA_END;
        } else {
          dres = new double[x.ia];
          double[] wd = w.asDoubleArr();
          double[] xd = x.asDoubleArr();
          for (i = 0; i < l; i++) dres[i] = fd.on(wd[i], xd[i]);
        }
        IA_END:
        double[] xd2 = x.asDoubleArr();
        while (i < x.ia) {
          dres[i] = fd.on(dres[i-l], xd2[i]);
          i++;
        }
        return new DoubleArr(dres, x.shape);
      }
    }
    MutVal res3 = new MutVal(x.shape, x);
    for (i = 0; i < l; i++) res3.set(i, f.call(w.get(i), x.get(i)));
    for (; i < x.ia; i++) res3.set(i, f.call(res3.get(i-l), x.get(i)));
    return res3.get();
  }
  
  public Value callInvX(Value f, Value w, Value x) {
    if (w.r()+1!=x.r() || !Arrays.equals(w.shape, Arrays.copyOfRange(x.shape, 1, x.r()))) throw new DomainError("F`⁼: 𝕨 must have shape 1↓≢𝕩", this);
    if (x.ia==0) return x;
    Value[] res = new Value[x.ia];
    int l = w.ia;
    int i = 0;
    for (; i < l; i++) res[i] = f.callInvX(w.get(i), x.get(i));
    for (; i < x.ia; i++) res[i] = f.callInvX(x.get(i-l), x.get(i));
    return Arr.create(res, x.shape);
  }
}