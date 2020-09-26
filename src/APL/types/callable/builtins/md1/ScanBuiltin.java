package APL.types.callable.builtins.md1;

import APL.errors.*;
import APL.tools.Pervasion;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.callable.Md1Derv;
import APL.types.callable.builtins.Md1Builtin;
import APL.types.callable.builtins.fns.*;

public class ScanBuiltin extends Md1Builtin {
  @Override public String repr() {
    return "`";
  }
  
  public Value call(Value f, Value x, Md1Derv derv) {
    if (x.ia == 0) return x;
    if (x.rank == 0) throw new DomainError("`: rank must be at least 1, argument was a scalar", this, x);
    int l = Arr.prod(x.shape, 1, x.shape.length);
    if (x.quickDoubleArr()) {
      Pervasion.NN2N fd = f.dyNum();
      if (fd != null) {
        final double[] dres;
        int i = l;
        ia: if (x.quickIntArr()) {
          if (x.rank==1 && x instanceof BitArr) {
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
              return new SingleItemArr(Num.ZERO, x.shape);
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
          int[] res = new int[x.ia];
          int[] xi = x.asIntArr();
          System.arraycopy(xi, 0, res, 0, l);
          while (i < x.ia) {
            double n = fd.on(res[i-l], xi[i]);
            if ((int)n != n) {
              dres = new double[x.ia]; for (int j = 0; j < dres.length; j++) dres[j] = res[j];
              dres[i++] = n;
              break ia;
            }
            res[i++] = (int) n;
          }
          return new IntArr(res, x.shape);
        } else dres = new double[x.ia];
        double[] xd = x.asDoubleArr();
        System.arraycopy(xd, 0, dres, 0, l);
        while (i < x.ia) {
          dres[i] = fd.on(dres[i-l], xd[i]);
          i++;
        }
        return new DoubleArr(dres, x.shape);
      }
    }
    Value[] res = new Value[x.ia];
    int i = 0;
    for (; i < l; i++) res[i] = x.get(i);
    for (; i < x.ia; i++) res[i] = f.call(res[i-l], x.get(i));
    return Arr.create(res, x.shape);
  }
  
  public Value call(Value f, Value w, Value x, Md1Derv derv) {
    int n = w.asInt();
    int len = x.ia;
    if (n < 0) throw new DomainError("`: 𝕨 should be non-negative (was "+n+")", this);
    if (x.rank > 1) throw new RankError("`: rank of 𝕩 should be less than 2 (was "+x.rank+")", this);
    
    if (x.quickDoubleArr()) {
      Value[] res = new Value[len-n+1];
      double[] xa = x.asDoubleArr();
      for (int i = 0; i < res.length; i++) {
        double[] curr = new double[n];
        System.arraycopy(xa, i, curr, 0, n);
        res[i] = f.call(new DoubleArr(curr));
      }
      return Arr.create(res);
    }
    
    Value[] res = new Value[len-n+1];
    Value[] xa = x.values();
    for (int i = 0; i < res.length; i++) {
      Value[] curr = new Value[n];
      // for (int j = 0; j < n; j++) curr[j] = wa[i + j];
      System.arraycopy(xa, i, curr, 0, n);
      res[i] = f.call(Arr.create(curr));
    }
    return Arr.create(res);
  }
}