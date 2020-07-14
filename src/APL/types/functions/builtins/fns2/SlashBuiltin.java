package APL.types.functions.builtins.fns2;

import APL.Main;
import APL.errors.*;
import APL.tools.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.mops.ReduceBuiltin;

public class SlashBuiltin extends Builtin {
  private static final Fun fn = new ReduceBuiltin().derive(new CeilingBuiltin());
  
  public String repr() {
    return "/";
  }
  
  
  
  public Value call(Value x) {
    int sum = (int) x.sum();
    if (x.rank == 1) {
      int[] sub = new int[sum];
      int p = 0;
      
      if (x instanceof BitArr) {
        BitArr.BR r = ((BitArr) x).read();
        for (int i = 0; i < x.ia; i++) {
          if (r.read()) sub[p++] = i;
        }
      } else {
        if (sum<0) for (Value v : x) if (v.asDouble() < 0) throw new DomainError("/: 𝕩 contained "+v, this, x);
        int[] xi = x.asIntArr();
        for (int i = 0; i < x.ia; i++) {
          int v = xi[i];
          if (v < 0) throw new DomainError("/: 𝕩 contained "+v, this, x);
          for (int j = 0; j < v; j++) {
            sub[p++] = i;
          }
        }
      }
      return new IntArr(sub);
    } else {
      int[] xi = x.asIntArr();
      if (Main.vind) { // •VI←1
        int[][] res = new int[x.rank][sum];
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
          } else if (n != 0) throw new DomainError("/: 𝕩 contained "+n, this, x);
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
          } else if (n != 0) throw new DomainError("/: 𝕩 contained "+n, this, x);
        }
        return new HArr(res);
      }
    }
  }
  
  public Value callInv(Value x) {
    int[] sh = fn.call(Num.ZERO, x).asIntVec();
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
  
  
  public static Value replicate(Value w, Value x, Callable blame) { // a lot of valuecopy
    if (x.rank==0) throw new RankError(blame+": 𝕩 cannot be scalar", blame, x);
    int depth = MatchBuiltin.full(w);
    int[][] am; // scalars are represented as 1-item int[]s
    if (w.ia == 0) {
      return x;
    } else if (depth <= 1) {
      if (w.rank==1 && w.ia!=x.shape[0]) throw new LengthError(blame+": wrong replicate length (length ≡ "+w.ia+", shape ≡ "+Main.formatAPL(x.shape)+")", blame);
      if (w instanceof BitArr && w.rank==1 && x.rank==1) {
        BitArr.BR r = ((BitArr) w).read();
        int sum = ((BitArr) w).isum();
        if (x.quickIntArr()) {
          int[] xi = x.asIntArr();
          int[] res = new int[sum]; int rp = 0;
          for (int i = 0; i < w.ia; i++) if (r.read()) res[rp++] = xi[i];
          return new IntArr(res);
        }
        Value[] res = new Value[sum]; int rp = 0;
        for (int i = 0; i < w.ia; i++) if (r.read()) res[rp++] = x.get(i);
        return Arr.create(res);
      }
      am = new int[1][];
      am[0] = w.asIntVec();
    } else {
      if (w.ia > x.rank) throw new DomainError(blame+": 𝕨 must have less items than ≠≢𝕩 ("+w.ia+" ≡ ≠𝕨, "+Main.formatAPL(x.shape)+" ≡ ≢𝕩)", blame, w);
      am = new int[w.ia][];
      for (int i = 0; i < w.ia; i++) {
        Value c = w.get(i);
        if (c.rank > 1) throw new RankError(blame+": depth 2 𝕨 cannot have rank "+c.rank+" items (contained shape "+Main.formatAPL(c.shape)+")", blame, w);
        if (c.rank==1 && c.ia!=x.shape[i]) throw new LengthError(blame+": wrong replicate length ("+c.ia+" ≡ ≠"+i+"⊏𝕨, shape ≡ "+Main.formatAPL(x.shape)+")", blame);
        am[i] = c.asIntArr();
      }
    }
    
    int rcam = 1; // result cell amount
    int[] rsh = new int[x.rank]; // result shape
    System.arraycopy(x.shape, am.length, rsh, am.length, x.shape.length-am.length);
    for (int i = 0; i < am.length; i++) {
      int s = 0;
      if (am[i].length == 1) s = am[i][0]*x.shape[i];
      else for (int k : am[i]) s+= k;
      rsh[i] = s;
      rcam*= s;
    }
    
    int csz = 1; // cell size to replicate
    for (int i = am.length; i < x.shape.length; i++) csz*= x.shape[i];
    MutVal res = new MutVal(rsh, x);
    
    recReplicate(res, 0, 0, 0, x.ia, x, x.shape, am);
    return res.get();
  }
  
  private static int recReplicate(MutVal res, int rpos, int ipos, int d, int rsz, Value x, int[] xsh, int[][] am) {
    if (d==am.length) {
      // System.arraycopy(x, ipos, res, rpos, rsz);
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
  
  // public Value callInvW(Value a, Value w) {
  //   if (a.rank!=1 || w.rank!=1) throw new DomainError("/⁼: dyadic inverting only possible on rank 1 arguments", this, a.rank!=1?a:w);
  //  
  // }
  
  public Value underW(Value o, Value w, Value x) {
    Value v = o instanceof Fun? ((Fun) o).call(call(w, x)) : o;
    if (MatchBuiltin.full(w)!=1) throw new DomainError("⌾/: 𝕨 of / must be a boolean vector", this, w);
    if (w.rank!=1 || x.rank!=1) throw new DomainError("⌾/: dyadic inverting only possible on rank 1 arguments", this, w.rank!=1? w : x);
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
      return new IntArr(res);
    }
    Value[] res = new Value[x.ia];
    for (int i = 0; i < wi.length; i++) {
      int d = wi[i];
      if (d!=0 && d!=1) throw new DomainError("⌾(𝕨⊸/): 𝕨 must be a boolean vector, contained "+Num.format(d));
      if (d == 1) res[i] = v.get(ipos++);
      else res[i] = x.get(i);
    }
    return Arr.create(res);
  }
}