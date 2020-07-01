package APL.types.functions.builtins.fns2;

import APL.*;
import APL.errors.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.mops.ReduceBuiltin;

import java.util.Arrays;

public class SlashBuiltin extends Builtin {
  private static final Fun fn = new ReduceBuiltin().derive(new CeilingBuiltin());
  
  public String repr() {
    return "/";
  }
  
  
  
  public Value call(Value w) {
    int sum = (int)w.sum();
    if (w.rank == 1) {
      if (sum<0) {
        for (Value v : w) if (v.asDouble() < 0) throw new DomainError("/: ⍵ contained "+v, this, w);
      }
      var sub = new double[sum];
      int p = 0;
      
      if (w instanceof BitArr) {
        BitArr.BR r = ((BitArr) w).read();
        for (int i = 0; i < w.ia; i++) {
          if (r.read()) sub[p++] = i;
        }
      } else {
        var da = w.asDoubleArr();
        for (int i = 0; i < w.ia; i++) {
          int v = (int) da[i];
          if (v < 0) throw new DomainError("/: ⍵ contained "+v, this, w);
          for (int j = 0; j < v; j++) {
            sub[p++] = i;
          }
        }
      }
      return new DoubleArr(sub);
    } else {
      double[] wd = w.asDoubleArr();
      if (Main.vind) { // •VI←1
        double[][] res = new double[w.rank][sum];
        int ri = 0;
        Indexer idx = new Indexer(w.shape);
        int rank = res.length;
        for (int i = 0; i < w.ia; i++) {
          int[] p = idx.next();
          int n = Num.toInt(wd[idx.pos()]);
          if (n > 0) {
            for (int k = 0; k < rank; k++) {
              for (int j = 0; j < n; j++) res[k][ri+j] = p[k];
            }
            ri+= n;
          } else if (n != 0) throw new DomainError("/: ⍵ contained "+n, this, w);
        }
        Value[] resv = new Value[rank];
        for (int i = 0; i < rank; i++) resv[i] = new DoubleArr(res[i]);
        return new HArr(resv);
      } else { // •VI←0
        Value[] res = new Value[sum];
        int ri = 0;
        Indexer idx = new Indexer(w.shape);
        for (int i = 0; i < w.ia; i++) {
          int[] p = idx.next();
          int n = Num.toInt(wd[idx.pos()]);
          if (n > 0) {
            DoubleArr pos = Main.toAPL(p);
            for (int j = 0; j < n; j++) res[ri++] = pos;
          } else if (n != 0) throw new DomainError("/: ⍵ contained "+n, this, w);
        }
        return new HArr(res);
      }
    }
  }
  
  public Value callInv(Value w) {
    int[] sh = fn.call(w).asIntVec();
    int ia = 1;
    for (int i = 0; i < sh.length; i++) {
      sh[i]+= 1;
      ia *= sh[i];
    }
    double[] arr = new double[ia];
    for (Value v : w) {
      int[] c = v.asIntVec();
      arr[Indexer.fromShape(sh, c)]++;
    }
    return new DoubleArr(arr, sh);
  }
  
  
  
  public Value call(Value a, Value w) {
    return replicate(a, w, this);
  }
  
  
  public static Value replicate(Value a, Value w, Callable blame) {
    if (a.rank == 0) {
      if (w.rank > 1) throw new RankError("⌿: rank of ⍵ should be ≤1 if ⍺ is a scalar", blame);
      int sz = a.asInt();
      if (sz < 0) {
        int am = w.ia*-sz;
        Value pr = w.prototype();
        if (pr instanceof Num) return new DoubleArr(new double[am]);
        Value[] res = new Value[am];
        Value n = w.first() instanceof Char? Char.SPACE : Num.ZERO;
        Arrays.fill(res, n);
        return Arr.create(res);
      }
      
      int am = w.ia*sz;
      if (w instanceof BitArr) {
        BitArr.BA res = new BitArr.BA(am);
        BitArr.BR r = ((BitArr) w).read();
        for (int i = 0; i < w.ia; i++) {
          if (r.read()) res.fill(sz);
          else          res.skip(sz);
        }
        return res.finish();
      }
      if (w.quickDoubleArr()) {
        double[] res = new double[am];
        double[] ds = w.asDoubleArr();
        int ptr = 0;
        for (int i = 0; i < w.ia; i++) {
          double c = ds[i];
          for (int j = 0; j < sz; j++) {
            res[ptr++] = c;
          }
        }
        return new DoubleArr(res);
      }
      Value[] res = new Value[am];
      int ptr = 0;
      for (int i = 0; i < w.ia; i++) {
        Value c = w.get(i);
        for (int j = 0; j < sz; j++) {
          res[ptr++] = c;
        }
      }
      return Arr.create(res);
    }
    
    // ⍺.rank ≠ 0
    if (a.rank != w.rank) throw new RankError("⌿: shapes of ⍺ & ⍵ must be equal (ranks "+a.rank+" vs "+w.rank + ")", blame);
    if (!Arrays.equals(a.shape, w.shape)) throw new LengthError("⌿: shapes of ⍺ & ⍵ must be equal ("+ Main.formatAPL(a.shape) + " vs " + Main.formatAPL(w.shape) + ")", blame);
    
    if (a instanceof BitArr) {
      BitArr ab = (BitArr) a;
      ab.setEnd(false);
      int sum = ab.isum();
      if (w instanceof BitArr) {
        BitArr.BA res = new BitArr.BA(sum);
        long[] wba = ((BitArr) w).arr;
        ((BitArr) a).setEnd(false);
        long[] aba = ((BitArr) a).arr;
        int ia = wba.length;
        
        for (int i = 0; i < ia; i++) {
          long wcb = wba[i];
          long acb = aba[i];
          for (int o = 0; o < 64; o++) {
            if ((acb&1)!=0) {
              res.add(wcb&1);
            }
            wcb>>= 1;
            acb>>= 1;
          }
        }
        return res.finish();
      }
      if (w.quickDoubleArr()) {
        if (sum > w.ia*.96) {
          double[] ds = w.asDoubleArr();
          double[] res = new double[sum];
          
          long[] la = ab.arr;
          int l = la.length;
          int am = 0, pos = 0;
          for (int i = 0; i < l; i++) {
            long c = la[i];
            for (int s = 0; s < 64; s++) {
              if ((c&1) == 0) {
                if (am != 0) System.arraycopy(ds, i*64 + s - am, res, pos, am);
                pos+= am;
                am = 0;
              } else am++;
              c>>= 1;
            }
          }
          if (am > 0) System.arraycopy(ds, ds.length - am, res, pos, am);
          return new DoubleArr(res);
        }
        double[] ds = w.asDoubleArr();
        double[] res = new double[sum];
        long[] la = ab.arr;
        int l = la.length;
        int pos = 0;
        for (int i = 0; i < l; i++) {
          long c = la[i];
          for (int s = 0; s < 64; s++) {
            if ((c&1) != 0) {
              res[pos++] = ds[i*64 + s];
            }
            c>>= 1;
          }
        }
        return new DoubleArr(res);
        // BitArr.BR r = ab.read();
        // int pos = 0;
        // for (int i = 0; i < w.ia; i++) {
        //   if (r.read()) {
        //     res[pos++] = ds[i];
        //   }
        // }
        // return new DoubleArr(res);
      }
      if (w instanceof ChrArr) {
        String ws = ((ChrArr) w).s;
        char[] chars = new char[sum];
        BitArr.BR r = ab.read();
        int pos = 0;
        for (int i = 0; i < w.ia; i++) {
          if (r.read()) {
            chars[pos++] = ws.charAt(i);
          }
        }
        return new ChrArr(chars);
      }
      Value[] res = new Value[sum];
      BitArr.BR r = ab.read();
      int pos = 0;
      for (int i = 0; i < w.ia; i++) {
        if (r.read()) {
          res[pos++] = w.get(i);
        }
      }
      return Arr.create(res);
    }
    
    
    int total = 0;
    int[] sizes = a.asIntArr();
    for (int i = 0; i < a.ia; i++) {
      total+= Math.abs(sizes[i]);
    }
    
    if (w instanceof BitArr) {
      BitArr.BA res = new BitArr.BA(total);
      BitArr.BR r = ((BitArr) w).read();
      for (int i = 0; i < w.ia; i++) {
        int am = sizes[i];
        if (r.read()) res.fill(am);
        else          res.skip(am);
      }
      return res.finish();
    }
    if (w.quickDoubleArr()) {
      int ptr = 0;
      double[] wi = w.asDoubleArr();
      double[] res = new double[total];
      for (int i = 0; i < a.ia; i++) {
        double c = wi[i];
        int am = sizes[i];
        if (sizes[i] < 0) {
          for (int j = 0; j > am; j--) {
            res[ptr++] = 0;
          }
        } else {
          for (int j = 0; j < am; j++) {
            res[ptr++] = c;
          }
        }
      }
      return new DoubleArr(res);
      
    } else {
      int ptr = 0;
      Value[] res = new Value[total];
      for (int i = 0; i < a.ia; i++) {
        Value c = w.get(i);
        int am = sizes[i];
        if (sizes[i] < 0) {
          am = -am;
          c = c.prototype();
        }
        for (int j = 0; j < am; j++) {
          res[ptr++] = c;
        }
      }
      return Arr.create(res);
    }
  }
}
