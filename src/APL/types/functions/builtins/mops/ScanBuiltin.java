package APL.types.functions.builtins.mops;

import APL.algs.Pervasion;
import APL.errors.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.*;
import APL.types.functions.builtins.fns2.*;

public class ScanBuiltin extends Mop {
  @Override public String repr() {
    return "`";
  }
  
  public Value call(Value f, Value x, DerivedMop derv) {
    Fun ff = f.asFun();
    if (x.ia == 0) return x;
    if (x.rank == 0) throw new DomainError("`: rank must be at least 1, 𝕩 was a scalar", this, x);
    int l = Arr.prod(x.shape, 1, x.shape.length);
    if (x.quickDoubleArr()) {
      Pervasion.NN2N fd = ff.dyNum();
      if (fd != null) {
        final double[] rd;
        int i = l;
        ia: if (x.quickIntArr()) {
          if (x.rank==1 && x instanceof BitArr && f instanceof OrBuiltin) {
            long[] arr = ((BitArr) x).arr;
            for (int j = 0; j < arr.length; j++) {
              long c = arr[j];
              if (c!=0) {
                long[] res = new long[arr.length];
                int sh = Long.numberOfTrailingZeros(c);
                res[j++] = -(1L << (sh));
                while (j < arr.length) res[j++] = ~0L;
                return new BitArr(res, x.shape);
              }
            }
            return new SingleItemArr(Num.ZERO, x.shape);
          }
          int[] ri = new int[x.ia];
          int[] xd = x.asIntArr();
          System.arraycopy(xd, 0, ri, 0, l);
          while (i < x.ia) {
            double n = fd.on(ri[i-l], xd[i]);
            if ((int)n != n) {
              rd = new double[x.ia]; for (int j = 0; j < rd.length; j++) rd[j] = ri[j];
              rd[i++] = n;
              break ia;
            }
            ri[i++] = (int) n;
          }
          return new IntArr(ri, x.shape);
        } else rd = new double[x.ia];
        double[] xd = x.asDoubleArr();
        System.arraycopy(xd, 0, rd, 0, l);
        while (i < x.ia) {
          rd[i] = fd.on(rd[i-l], xd[i]);
          i++;
        }
        return new DoubleArr(rd, x.shape);
      } else if (x instanceof BitArr && ff instanceof NEBuiltin && x.rank==1) {
        long[] arr = ((BitArr) x).arr;
        long[] res = new long[arr.length];
        long xor = 0;
        for (int i = 0; i < arr.length; i++) {
          long c = arr[i];
          long r = c ^ (c<<1);
          r^= r<< 2; r^= r<< 4; r^= r<<8;
          r^= r<<16; r^= r<<32; r^=  xor;
          res[i] = r;
          xor = r>>63; // copies sign bit
        }
        return new BitArr(res, x.shape);
      }
    }
    Value[] res = new Value[x.ia];
    int i = 0;
    for (; i < l; i++) res[i] = x.get(i);
    for (; i < x.ia; i++) res[i] = ff.call(res[i-l], x.get(i));
    return Arr.create(res, x.shape);
  }
  
  public Value call(Value f, Value w, Value x, DerivedMop derv) {
    Fun ff = f.asFun();
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
        res[i] = ff.call(new DoubleArr(curr));
      }
      return Arr.create(res);
    }
    
    Value[] res = new Value[len-n+1];
    Value[] xa = x.values();
    for (int i = 0; i < res.length; i++) {
      Value[] curr = new Value[n];
      // for (int j = 0; j < n; j++) curr[j] = wa[i + j];
      System.arraycopy(xa, i, curr, 0, n);
      res[i] = ff.call(Arr.create(curr));
    }
    return Arr.create(res);
  }
}