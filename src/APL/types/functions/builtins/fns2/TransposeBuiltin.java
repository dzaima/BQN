package APL.types.functions.builtins.fns2;

import APL.errors.*;
import APL.tools.Indexer;
import APL.types.*;
import APL.types.arrs.DoubleArr;
import APL.types.functions.Builtin;

import java.util.Arrays;

public class TransposeBuiltin extends Builtin {
  @Override public String repr() {
    return "⍉";
  }
  
  public Value call(Value x) {
    if (x.scalar()) return x;
    int r = x.rank;
    int[] sh = new int[r];
    int n = 1;
    for (int i = 0; i < r-1; i++) {
      n*= sh[i] = x.shape[i+1];
    }
    int m = sh[r-1] = x.shape[0];
    return matTrans(x, m, n, sh);
  }
  public Value callInv(Value x) {
    if (x.scalar()) return x;
    int r = x.rank;
    int[] sh = new int[r];
    int n = sh[0] = x.shape[r-1];
    int m = 1;
    for (int i = 1; i < r; i++) {
      m*= sh[i] = x.shape[i-1];
    }
    return matTrans(x, m, n, sh);
  }
  
  static Value matTrans(Value x, int m, int n, int[] sh) {
    if (m==0 || n==0) return x.ofShape(sh);
    if (x.quickDoubleArr()) {
      double[] xd = x.asDoubleArr();
      double[] res = new double[x.ia];
      int ip = 0;
      for (int cx = 0; cx < m; cx++) {
        int op = cx;
        for (int cy = 0; cy < n; cy++) {
          res[op] = xd[ip++];
          op+= m;
        }
      }
      return new DoubleArr(res, sh);
    } else {
      Value[] res = new Value[x.ia];
      int ip = 0;
      for (int cx = 0; cx < m; cx++) {
        int op = cx;
        for (int cy = 0; cy < n; cy++) {
          res[op] = x.get(ip++);
          op+= m;
        }
      }
      return Arr.create(res, sh);
    }
  }
  
  int posMin(int a, int b) {
    return a<0 ? b : Math.min(a,b);
  }
  
  public Value call(Value w, Value x) {
    int[] ts = w.asIntVec();
    int l = ts.length;
    if (l == 0) return x.scalar()? x.ofShape(new int[]{}) : x;
    int r = x.rank;
    if (l > r) throw new RankError("⍉: Length of 𝕨 ("+l+") exceeded rank of 𝕩 ("+r+")", this);
    
    // compute shape for the given axes
    int[] t = new int[r];
    System.arraycopy(ts, 0, t, 0, l);
    int[] sh = new int[r];
    for (int i = 0; i < r; i++) sh[i] = -1;
    for (int i = 0; i < l; i++) {
      int a = t[i];
      if (a<0 || a>=r) throw new RankError("⍉: Axis "+a+" does not exist (rank of 𝕩 is "+r+")", this);
      sh[a] = posMin(sh[a], x.shape[i]);
    }
    
    // fill in remaining axes and check for missing ones
    int k = 0;
    for (int i = l; i < r; i++,k++) {
      while (sh[k] >= 0) k++;
      t[i] = k;
      sh[k] = x.shape[i];
    }
    while (k<r && sh[k]>=0) k++;
    for (int i = k; i < r; i++) {
      if (sh[i] >= 0) throw new DomainError("⍉: Missing output axis "+k, this);
    }
    
    sh = Arrays.copyOf(sh, k);
    Value[] res = new Value[Arr.prod(sh)];
    for (int[] c : new Indexer(sh)) {
      int[] d = new int[r];
      for (int i = 0; i < r; i++) {
        d[i] = c[t[i]];
      }
      res[Indexer.fromShape(sh, c)] = x.simpleAt(d);
    }
    return Arr.create(res, sh);
  }
  
  public Value callInvW(Value w, Value x) {
    int[] ts = w.asIntVec();
    int l = ts.length;
    if (l == 0) {
      if (x.scalar()) throw new DomainError("⍉⁼: Result of ⍉ must be an array");
      return x;
    }
    int r = x.rank;
    if (l > r) throw new RankError("⍉⁼: Length of 𝕨 ("+l+") exceeded rank of 𝕩 ("+r+")", this);
    
    // fill trailing axes
    int[] t = new int[r];
    System.arraycopy(ts, 0, t, 0, l);
    boolean[] oc = new boolean[r];  // occupied
    for (int i = 0; i < l; i++) {
      int a = t[i];
      if (a<0 || a>=r) throw new RankError("⍉: Axis "+a+" does not exist (rank of 𝕩 is "+r+")", this);
      if (oc[a]) throw new RankError("⍉⁼: Repeated axis: "+a, this);
      oc[a] = true;
    }
    for (int i=l,k=0; i < r; i++,k++) {
      while (oc[k]) k++;
      t[i] = k;
    }

    int[] sh = new int[r];
    for (int i = 0; i < r; i++) sh[i] = x.shape[t[i]];
    Value[] res = new Value[x.ia];
    for (int[] c : new Indexer(sh)) {
      int[] d = new int[r];
      for (int i = 0; i < r; i++) {
        d[t[i]] = c[i];
      }
      res[Indexer.fromShape(sh, c)] = x.simpleAt(d);
    }
    return Arr.create(res, sh);
  }
}
