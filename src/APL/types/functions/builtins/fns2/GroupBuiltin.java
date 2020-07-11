package APL.types.functions.builtins.fns2;

import APL.Main;
import APL.errors.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;

import java.util.*;

public class GroupBuiltin extends Builtin {
  public String repr() {
    return "⊔";
  }
  
  
  private static class MutDA {
    double[] ds = new double[4];
    int sz;
    void add(double i) {
      if (sz >= ds.length) {
        ds = Arrays.copyOf(ds, ds.length*2);
      }
      ds[sz] = i;
      sz++;
    }
  }
  
  public Value call(Value x) {
    if (x.rank != 1) throw new RankError("⊔: argument must be a vector", this, x);
    int depth = MatchBuiltin.full(x);
    if (depth == 1) {
      int[] xi = x.asIntVec();
      int sz = 0;
      for (int d : xi) sz = Math.max(sz, d);
      sz++;
      
      MutDA[] ds = new MutDA[sz];
      for (int i = 0; i < sz; i++) ds[i] = new MutDA();
      for (int i = 0; i < xi.length; i++) {
        int c = xi[i];
        if (c>=0) ds[c].add(i);
        else if (c!=-1) throw new DomainError("⊔: didn't expect "+c+" in argument", this, x);
      }
      Value[] res = new Value[sz];
      for (int i = 0; i < sz; i++) res[i] = new DoubleArr(Arrays.copyOf(ds[i].ds, ds[i].sz));
      return new HArr(res);
    }
    if (depth != 2) throw new DomainError("⊔: argument must be depth 1 or 2 (was "+depth+")", this, x);
    double[] args = new double[x.ia];
    for (int i = 0; i < args.length; i++) {
      Value c = x.get(i);
      if (c.rank != 1) throw new DomainError("⊔: expected items of argument to be vectors (contained item with shape "+Main.formatAPL(c.shape)+")", this, x);
      args[i] = c.ia;
    }
    return call(x, UDBuiltin.on(new DoubleArr(args), null)); // gives strange errors but whatever
  }
  
  
  
  @SuppressWarnings("unchecked") // no. bad java. bad.
  public Value call(Value w, Value x) {
    int depth = MatchBuiltin.full(w);
    if (depth > 2) throw new DomainError("⊔: depth of 𝕨 must be at most 2 (was "+depth+")", this, w);
    int rank = w.ia;
    if (x.rank == 1) {
      int[] poss;
      if (depth == 2) {
        if (w.rank!=1 || rank!=1) throw new RankError("⊔: expected a depth 2 𝕨 to be a 1-item vector (had shape "+Main.formatAPL(w.shape)+")", this, w);
        poss = w.get(0).asIntVec();
      } else poss = w.asIntVec();
      int sz = -1;
      for (int i : poss) sz = Math.max(sz, i);
      sz++;
      ArrayList<Value>[] vs = new ArrayList[sz];
      for (int i = 0; i < sz; i++) vs[i] = new ArrayList<>();
      for (int i = 0; i < x.ia; i++) {
        int c = poss[i];
        if (c>=0) {
          vs[c].add(x.get(i));
        } else if (c!=-1) throw new DomainError("⊔: didn't expect "+c+" in 𝕨", this, w);
      }
      Value[] res = new Value[sz];
      for (int i = 0; i < sz; i++) res[i] = Arr.create(vs[i].toArray(new Value[0]));
      return new HArr(res);
    }
    
    if (w.rank > 1) throw new RankError("⊔: 𝕨 must be vector or scalar, had rank "+w.rank, this, w);
    int[][] wa = new int[rank][];
    for (int i = 0; i < rank; i++) wa[i] = w.get(i).asIntVec();
    int[] rsh = new int[rank];
    for (int i = 0; i < rank; i++) {
      int max = -1;
      for (int c : wa[i]) max = Math.max(max, c);
      rsh[i] = max+1;
    }
    int sz = Arr.prod(rsh);
    int[][] rshs = new int[sz][rank];
    int repl = 1;
    for (int i = rank-1; i>=0; i--) {
      int[] ca = new int[rsh[i]];
      for (int c : wa[i]) {
        if (c>=0) ca[c]++;
        else if (c!=-1) throw new DomainError("⊔: didn't expect "+c+" in 𝕨", this, w);
      }
      int rp = 0;
      while (rp < sz) {
        for (int c : ca) {
          for (int k = 0; k < repl; k++) rshs[rp++][i] = c;
        }
      }
      repl*= rsh[i];
    }
  
    Value[][] vs = new Value[sz][];
    for (int i = 0; i < sz; i++) vs[i] = new Value[Arr.prod(rshs[i])];
    recIns(vs, new int[sz], rsh, 0, 0, 0, wa, x);
    
    Value[] res = new Value[sz];
    for (int i = 0; i < sz; i++) res[i] = Arr.create(vs[i], rshs[i]);
    return new HArr(res, rsh);
  }
  
  private void recIns(Value[][] vs, int[] ram, int[] rsh, int rp, int k, int ip, int[][] w, Value x) {
    if (k == rsh.length) {
      vs[rp][ram[rp]++] = x.get(ip);
    } else {
      rp*= rsh[k];
      ip*= x.shape[k];
      int[] c = w[k];
      for (int i = 0; i < c.length; i++) {
        if (c[i] >= 0) recIns(vs, ram, rsh, rp+c[i], k+1, ip+i, w, x);
      }
    }
  }
}