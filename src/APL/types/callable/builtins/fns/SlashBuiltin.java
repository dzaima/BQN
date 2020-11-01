package APL.types.callable.builtins.fns;

import APL.Main;
import APL.errors.*;
import APL.tools.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.callable.builtins.FnBuiltin;
import APL.types.callable.builtins.md1.FoldBuiltin;

import java.util.Arrays;

public class SlashBuiltin extends FnBuiltin {
  private static final Value fn = new FoldBuiltin().derive(new CeilingBuiltin());
  
  public String ln(FmtInfo f) { return "/"; }
  
  public Value call(Value x) {
    return on(x, this);
  }
  public static Value on(Value x, Callable blame) {
    int sum = (int) x.sum();
    if (x.r() == 1) {
      int[] sub = new int[sum];
      int p = 0;
      
      if (x instanceof BitArr) {
        BitArr.BR xr = ((BitArr) x).read();
        for (int i = 0; i < x.ia; i++) {
          if (xr.read()) sub[p++] = i;
        }
      } else {
        if (sum<0) for (Value v : x) if (v.asDouble() < 0) throw new DomainError(blame+": argument contained "+v, blame, x);
        int[] xi = x.asIntArr();
        for (int i = 0; i < x.ia; i++) {
          int v = xi[i];
          if (v < 0) throw new DomainError(blame+": argument contained "+v, blame, x);
          for (int j = 0; j < v; j++) {
            sub[p++] = i;
          }
        }
      }
      return new IntArr(sub);
    } else {
      int[] xi = x.asIntArr();
      if (Main.vind) { // •VI←1
        int[][] res = new int[x.r()][sum];
        int ri = 0;
        Indexer idx = new Indexer(x.shape);
        int rank = res.length;
        for (int i = 0; i < x.ia; i++) {
          int[] p = idx.next();
          int n = xi[idx.pos()];
          if (n > 0) {
            for (int k = 0; k < rank; k++) {
              for (int j = 0; j < n; j++) res[k][ri+j] = p[k];
            }
            ri+= n;
          } else if (n != 0) throw new DomainError(blame+": argument contained "+n, blame, x);
        }
        Value[] resv = new Value[rank];
        for (int i = 0; i < rank; i++) resv[i] = new IntArr(res[i]);
        return new HArr(resv);
      } else { // •VI←0
        Value[] res = new Value[sum];
        int ri = 0;
        Indexer idx = new Indexer(x.shape);
        for (int i = 0; i < x.ia; i++) {
          int[] p = idx.next();
          int n = xi[idx.pos()];
          if (n > 0) {
            Arr pos = new IntArr(p.clone());
            for (int j = 0; j < n; j++) res[ri++] = pos;
          } else if (n != 0) throw new DomainError(blame+": argument contained "+n, blame, x);
        }
        return new HArr(res);
      }
    }
  }
  
  public Value callInv(Value x) {
    int[] sh = fn.call(Num.MINUS_ONE, x).asIntVec();
    int ia = 1;
    for (int i = 0; i < sh.length; i++) { sh[i]+= 1; ia*= sh[i]; }
    int[] arr = new int[ia];
    if (x.quickDoubleArr()) {
      for (int c : x.asIntArr()) arr[c]++;
    } else {
      for (Value v : x) arr[Indexer.fromShape(sh, v.asIntVec())]++;
    }
    return new IntArr(arr, sh);
  }
  
  
  
  public Value call(Value w, Value x) {
    return replicate(w, x, this);
  }
  
  // static byte[] idxds = new byte[256*8];
  // static {
  //   for (int i = 0; i < 256; i++) {
  //     int o = 0;
  //     int rp = 0;
  //     for (int j = 0; j < 8; j++) {
  //       boolean c = (i>>j & 1) != 0;
  //       if (c) {
  //         idxds[i*8 + rp++] = (byte) o;
  //         o = 1;
  //       } else o++;
  //     }
  //   }
  // }
  //
  // static byte[] idxs = new byte[256*8];
  // static {
  //   for (int i = 0; i < 256; i++) {
  //     int rp = 0;
  //     for (byte j = 0; j < 8; j++) {
  //       boolean c = (i>>j & 1) != 0;
  //       if (c) idxs[i*8 + rp++] = j;
  //     }
  //   }
  // }
  
  // private static final byte[] sbuf = new byte[256];
  // private static byte[] indbuf = new byte[256];
  public static Value replicate(Value w, Value x, Callable blame) { // a lot of valuecopy
    if (x.r()==0) throw new RankError(blame+": 𝕩 cannot be scalar", blame, x);
    int depth = MatchBuiltin.full(w);
    if (w.r() > 1) {
      if (!Main.vind) throw new DomainError(blame+": 𝕨 must have rank≤1 (was shape "+Main.formatAPL(w.shape)+")", blame);
      if (w.r() != x.r()) throw new DomainError(blame+": if 1<=𝕨 then 𝕨 and 𝕩 must have equal ranks ("+w.r()+" vs "+x.r()+")", blame);
      if (!Arrays.equals(w.shape, x.shape)) throw new DomainError(blame+": if 1<=𝕨 then 𝕨 and 𝕩 must have equal shapes ("+Main.formatAPL(w.shape)+" vs "+Main.formatAPL(x.shape)+")", blame);
      int[] sh = {w.ia};
      w = w.ofShape(sh);
      x = x.ofShape(sh);
    }
    int[][] am; // scalars are represented as 1-item int[]s
    if (w.ia == 0) { // TODO reduce empty dimensions as if it were replicated
      return x;
    } else if (depth <= 1) {
      if (w.r()==1 && w.ia!=x.shape[0]) throw new LengthError(blame+": wrong replicate length (length ≡ "+w.ia+", shape ≡ "+Main.formatAPL(x.shape)+")", blame);
      if (w instanceof BitArr && w.r()==1 && x.r()==1) {
        BitArr wb = (BitArr) w;
        wb.setEnd(false);
        long[] wl = ((BitArr) w).arr;
        
        
        
        // ========================= BRANCHING PER-BIT =========================
        int sum = ((BitArr) w).isum();
        if (x.quickIntArr()) {
          int[] res = new int[sum];
          int[] xv = x.asIntArr();
          int rp=0, ip=0;
          for (long l : wl) {
            for (int j = 0; j < 64; j++) {
              if ((l&1)!=0) res[rp++] = xv[ip];
              l>>= 1;
              ip++;
            }
          }
          return new IntArr(res);
        }
        Value[] res = new Value[sum];
        Value[] xv = x.values();
        int rp=0, ip=0;
        for (long l : wl) {
          for (int j = 0; j < 64; j++) {
            if ((l&1)!=0) res[rp++] = xv[ip];
            l>>= 1;
            ip++;
          }
        }
        return Arr.create(res);
        
        
        
        // ========================= BRANCHLESS PER-BIT =========================
        // note: incomplete (doesn't handle long runs of trailing 0s)
        // int sum = ((BitArr) w).isum();
        // Value[] res = new Value[sum];
        // int rp=0, ip=0;
        // for (int i = 0; i < wl.length-1; i++) {
        //   long l = wl[i];
        //   for (int j = 0; j < 64; j++) {
        //     res[rp] = xv[ip++];
        //     rp+= l&1;
        //     l>>= 1;
        //   }
        // }
        // long last = wl[wl.length-1];
        // int end = 64-Long.numberOfLeadingZeros(last);
        // for (int j = 0; j < end; j++) { // wl is guaranteed to gave >=1 item due to if (w.ia ==0) earlier
        //   res[rp] = xv[ip++];
        //   rp+= last&1;
        //   last>>= 1;
        // }
        // return new HArr(res);
        
        
        
        
        
        
        // ========================= CUMULATIVE =========================
        // note: incomplete (doesn't handle runs of 0s with length>255)
        // if (indbuf.length<x.ia+1) indbuf = new byte[x.ia+1];
        // byte[] buf = indbuf;
        // buf[0] = 0;
        // int bp = 0;
        // for (long cl : wl) {
        //   for (int j = 0; j < 64; j+= 8) {
        //     int b = (int) (cl>>j & 0xff);
        //     int pop = Integer.bitCount(b);
        //     byte prev = buf[bp];
        //     // for (int i = 0; i < 8; i++) buf[bp+i] = idxds[b*8 + i];
        //     System.arraycopy(idxds, b*8, buf, bp, 8);
        //     buf[bp+8] = 0;
        //     buf[bp]+= prev;
        //     bp+= pop;
        //     buf[bp]+= Math.min(8, Integer.numberOfLeadingZeros(b)-23);
        //   }
        // }
        // Value[] res = new Value[bp];
        // int rp = 0;
        // for (int i = 0; i < bp; i++) {
        //   rp+= buf[i];
        //   res[i] = xv[rp];
        // }
        // return new HArr(res);
        
        
        
        
        
        
        
        
        
        
        
        
        // ========================= INDEX LOOKUP TABLE =========================
        // int sum = 0;
        // for (long l : wl) sum+= Long.bitCount(l);
        // Value[] res = new Value[sum];
        // int bp = 0;
        // byte ip = 0;
        // for (long cl : wl) {
        //   for (int j = 0; j < 64; j+= 8) {
        //     int b = (int) (cl>>j & 0xff);
        //     int pop = Integer.bitCount(b);
        //     for (int i = 0; i < 8; i++) sbuf[bp+i] = (byte) (ip + idxs[b*8 + i]);
        //     ip+= 8;
        //     bp+= pop;
        //   }
        // }
        // Value[] res = new Value[bp];
        // for (int i = 0; i < res.length; i++) res[i] = xv[sbuf[i] & 0xff];
        // return new HArr(res);
        
        
        
        
        // index lookup table, fixed, probably
        // Value[] res = new Value[wb.isum()];
        // int rp = 0;
        // int ip = 0;
        // for (long cl : wl) {
        //   int bp = 0;
        //   for (int j = 0; j < 64; j+= 8) {
        //     int b = (int) (cl>>j & 0xff);
        //     int pop = Integer.bitCount(b);
        //     for (int i = 0; i < 8; i++) sbuf[bp+i] = (byte) (ip+idxs[b*8 + i]);
        //     ip+= 8;
        //     bp+= pop;
        //   }
        //   for (int i = 0; i < bp; i++) {
        //     res[i+rp] = xv[sbuf[i]&0xff];
        //   }
        //   rp+= bp;
        // }
        // return new HArr(res);
        
        
        
        // original code
        // BitArr.BR r = ((BitArr) w).read();
        // int sum0 = ((BitArr) w).isum();
        // if (x.quickIntArr()) {
        //   int[] xi = x.asIntArr();
        //   int[] res = new int[sum]; int rp = 0;
        //   for (int i = 0; i < w.ia; i++) if (r.read()) res[rp++] = xi[i];
        //   return new IntArr(res);
        // }
        // Value[] res0 = new Value[sum0]; int rp0 = 0;
        // for (int i = 0; i < w.ia; i++) if (r.read()) res0[rp0++] = xv[i];
        // return Arr.create(res0);
      }
      am = new int[1][];
      am[0] = w.asIntVec();
    } else {
      if (w.ia > x.r()) throw new DomainError(blame+": 𝕨 must have less items than ≠≢𝕩 ("+w.ia+" ≡ ≠𝕨, "+Main.formatAPL(x.shape)+" ≡ ≢𝕩)", blame, w);
      am = new int[w.ia][];
      for (int i = 0; i < w.ia; i++) {
        Value c = w.get(i);
        if (c.r() > 1) throw new RankError(blame+": depth 2 𝕨 cannot have rank "+c.r()+" items (contained shape "+Main.formatAPL(c.shape)+")", blame, w);
        if (c.r()==1 && c.ia!=x.shape[i]) throw new LengthError(blame+": wrong replicate length ("+c.ia+" ≡ ≠"+i+"⊏𝕨, shape ≡ "+Main.formatAPL(x.shape)+")", blame);
        am[i] = c.asIntArr();
      }
    }
    
    int[] rsh = new int[x.r()]; // result shape
    System.arraycopy(x.shape, am.length, rsh, am.length, x.r()-am.length);
    for (int i = 0; i < am.length; i++) {
      int s = 0;
      if (am[i].length == 1) s = am[i][0]*x.shape[i];
      else for (int k : am[i]) s+= k;
      rsh[i] = s;
    }
    
    MutVal res = new MutVal(rsh, x);
    
    recReplicate(res, 0, 0, 0, x.ia, x, x.shape, am);
    return res.get();
  }
  
  private static int recReplicate(MutVal res, int rpos, int ipos, int d, int rsz, Value x, int[] xsh, int[][] am) {
    if (d==am.length) {
      res.copy(x, ipos, rpos, rsz);
      return rpos+rsz;
    } else {
      int[] c = am[d];
      if (xsh[d] != 0) rsz/= xsh[d];
      if (c.length == 1) {
        int a = c[0];
        for (int i = 0; i < xsh[d]; i++) {
          for (int j = 0; j < a; j++) rpos = recReplicate(res, rpos, ipos, d+1, rsz, x, xsh, am);
          ipos+= rsz;
        }
      } else {
        for (int a : c) {
          for (int j = 0; j < a; j++) rpos = recReplicate(res, rpos, ipos, d+1, rsz, x, xsh, am);
          ipos+= rsz;
        }
      }
      return rpos;
    }
  }
  
  // public Value callInvX(Value a, Value w) {
  //   if (a.rank!=1 || w.rank!=1) throw new DomainError("/⁼: dyadic inverting only possible on rank 1 arguments", this, a.rank!=1?a:w);
  //  
  // }
  
  public Value underW(Value o, Value w, Value x) {
    Value v = o instanceof Fun? o.call(call(w, x)) : o;
    if (MatchBuiltin.full(w)!=1) throw new NYIError("⌾/: 𝕨 of / must be a boolean vector", this, w);
    int[] sh;
    if (w.r() > 1) {
      if (!Main.vind) throw new DomainError("⌾/: 𝕨 must have rank≤1 (was shape "+Main.formatAPL(w.shape)+")", this);
      if (w.r() != x.r()) throw new DomainError("⌾/: if 1<=𝕨 then 𝕨 and 𝕩 must have equal ranks ("+w.r()+" vs "+x.r()+")", this);
      if (!Arrays.equals(w.shape, x.shape)) throw new DomainError("⌾/: if 1<=𝕨 then 𝕨 and 𝕩 must have equal shapes ("+Main.formatAPL(w.shape)+" vs "+Main.formatAPL(x.shape)+")", this);
      sh = w.shape;
      int[] fsh = {w.ia};
      w = w.ofShape(fsh);
      x = x.ofShape(fsh);
    } else sh = w.shape;
    if (w.r()!=1 || x.r()!=1) throw new DomainError("⌾/: dyadic inverting only possible on rank 1 arguments", this, w.r()!=1? w : x);
    double asum = w.sum();
    if (asum != v.ia) throw new LengthError("𝕗⌾/: expected 𝕗 to not change shape (was "+asum+", got "+Main.formatAPL(v.shape)+")", this, x);
    int ipos = 0;
    int[] wi = w.asIntArr();
    
    if (x.quickIntArr() && v.quickIntArr()) {
      int[] vi = v.asIntArr();
      int[] xi = x.asIntArr();
      int[] res = new int[x.ia];
      for (int i = 0; i < wi.length; i++) {
        int d = wi[i];
        if (d!=0 && d!=1) throw new DomainError("⌾(𝕨⊸/): 𝕨 must be a boolean vector, contained "+Num.format(d));
        if (d == 1) res[i] = vi[ipos++];
        else res[i] = xi[i];
      }
      return new IntArr(res, sh);
    }
    Value[] res = new Value[x.ia];
    for (int i = 0; i < wi.length; i++) {
      int d = wi[i];
      if (d!=0 && d!=1) throw new DomainError("⌾(𝕨⊸/): 𝕨 must be a boolean vector, contained "+Num.format(d));
      if (d == 1) res[i] = v.get(ipos++);
      else res[i] = x.get(i);
    }
    return Arr.create(res, sh);
  }
}