package BQN.types.callable.builtins.fns;

import BQN.Main;
import BQN.errors.*;
import BQN.tools.*;
import BQN.types.*;
import BQN.types.arrs.*;
import BQN.types.callable.builtins.FnBuiltin;

import java.util.Arrays;

public class GroupBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "⊔"; }
  
  public static class MutIA {
    public int[] ds = new int[4];
    public int sz;
    public void add(int i) {
      if (sz >= ds.length) {
        ds = Arrays.copyOf(ds, ds.length*2);
      }
      ds[sz] = i;
      sz++;
    }
  }
  
  public Value call(Value x) {
    if (x.r() != 1) throw new RankError("⊔: argument must be a vector", this);
    int depth = MatchBuiltin.full(x);
    if (depth == 1) {
      int[] xi = x.asIntVec();
      int sz = -1;
      for (int d : xi) sz = Math.max(sz, d);
      sz++;
      
      MutIA[] ds = new MutIA[sz];
      for (int i = 0; i < sz; i++) ds[i] = new MutIA();
      for (int i = 0; i < xi.length; i++) {
        int c = xi[i];
        if (c>=0) ds[c].add(i);
        else if (c!=-1) throw new DomainError("⊔: didn't expect "+c+" in argument", this);
      }
      Value[] res = new Value[sz];
      for (int i = 0; i < sz; i++) res[i] = new IntArr(Arrays.copyOf(ds[i].ds, ds[i].sz));
      if (sz==0) return new EmptyArr(EmptyArr.SHAPE0, new EmptyArr(EmptyArr.SHAPE0, Num.ZERO));
      return new HArr(res);
    }
    if (depth != 2) throw new DomainError("⊔: argument must be depth 1 or 2 (was "+depth+")", this);
    int[] args = new int[x.ia];
    for (int i = 0; i < args.length; i++) {
      Value c = x.get(i);
      if (c.r() != 1) throw new DomainError("⊔: expected items of argument to be vectors (contained item with shape "+Main.fArr(c.shape)+")", this);
      args[i] = c.ia;
    }
    return call(x, UDBuiltin.on(new IntArr(args), null)); // gives strange errors but whatever
  }
  
  
  
  public Value call(Value w, Value x) {
    int depth = MatchBuiltin.full(w);
    int[][] wp;
    int wsz;
    int xsz = x.r();
    int max = -1;
    if (depth <= 1) {
      wsz = 1;
      if (w.r() != 1) {
        if (x.shape.length<w.r() || !Arr.eqPrefix(w.shape, x.shape, w.r())) throw new RankError("⊔: shape of depth 1 rank "+w.r()+" 𝕨 must be a prefix of 𝕩 ("+Main.fArr(w.shape)+" ≡ ≢𝕨; "+Main.fArr(x.shape)+" ≡ ≢𝕩)");
        int[] xsh;
        if (w.r()==0) {
          xsh = new int[x.r()+1]; System.arraycopy(x.shape, 0, xsh, 1, x.r());
        } else {
          xsh = Arrays.copyOfRange(x.shape, w.r()-1, x.r());
        }
        xsh[0] = w.ia;
        x = x.ofShape(xsh);
        w = w.ofShape(new int[]{w.ia});
        xsz = x.r();
      }
      if (xsz==0) throw new RankError("⊔: 𝕩 cannot be scalar if 𝕨 has depth 1", this);
      int[] wi = w.asIntArr();
      if (w.ia != x.shape[0]) {
        if (w.ia != x.shape[0]+1) throw new LengthError("⊔: length of 𝕨 must be one of 0‿1+⊑≢𝕩 ("+w.ia+" ≡ ≠𝕨; "+Main.fArr(x.shape)+" ≡ ≢𝕩)", this);
        max = wi[wi.length-1];
        if (max<-1) throw new DomainError("⊔: didn't expect "+max+" in 𝕨", this);
        wp = new int[][]{Arrays.copyOf(wi, w.ia-1)};
        for (int c : wp[0]) if (c >= max) throw new LengthError("⊔: tail element of 𝕨 must be the biggest", this);
      } else wp = new int[][]{wi};
    } else if (depth == 2) {
      wsz = w.ia;
      if (w.r() > 1) throw new RankError("⊔: depth 2 𝕨 must have rank ≤1 (had shape "+Main.fArr(w.shape)+")", this);
      if (wsz > xsz) throw new DomainError("⊔: length of depth 2 𝕨 must be greater than rank of 𝕩 ("+wsz+" ≡ ≠𝕨; "+Main.fArr(x.shape)+" ≡ ≢𝕩)", this);
      wp = new int[wsz][];
      for (int i = 0; i < wsz; i++) {
        Value c = w.get(i);
        if (c.r()!=1) throw new RankError("⊔: items of 𝕨 must be of rank 1", this);
        wp[i] = c.asIntArr();
        if (c.ia != x.shape[i]) { int[] shs = new int[w.ia]; for (int j = 0; j < w.ia; j++) shs[j] = w.get(j).ia;
          throw new LengthError("⊔: lengths of 𝕨 must be a prefix of ≢𝕩 ("+Main.fArr(shs)+" ≡ ≠¨𝕨; "+Main.fArr(x.shape)+" ≡ ≢𝕩)", this); }
      }
    } else throw new DomainError("⊔: depth of 𝕨 must be 1 or 2 (was "+depth+")", this);
    
    
    
    if (x.r()==1) { // fast path
      int[] poss = wp[0];
      int sz = -1;
      if (max==-1) {
        for (int c : poss) sz = Math.max(sz, c);
        sz++;
      } else sz = max;
      if (sz==0) return new EmptyArr(EmptyArr.SHAPE0, new EmptyArr(EmptyArr.SHAPE0, x.fItemS()));
      int[] rshs = new int[sz];
      for (int c : poss) {
        if (c>=0) rshs[c]++;
        else if (c!=-1) throw new DomainError("⊔: didn't expect "+c+" in 𝕨", this);
      }
      if (x.quickIntArr()) {
        int[] xi = x.asIntArr();
        int[] idxs = new int[sz];
        int[][] vs = new int[sz][];
        for (int i = 0; i < sz; i++) vs[i] = new int[rshs[i]];
        for (int i = 0; i < x.ia; i++) {
          int c = poss[i];
          if (c>=0) vs[c][idxs[c]++] = xi[i];
        }
        Value[] res = new Value[sz];
        for (int i = 0; i < sz; i++) res[i] = new IntArr(vs[i]);
        return new HArr(res);
      }
      if (x instanceof ChrArr) {
        String xs = ((ChrArr)x).s;
        int[] idxs = new int[sz];
        char[][] vs = new char[sz][];
        for (int i = 0; i < sz; i++) vs[i] = new char[rshs[i]];
        for (int i = 0; i < x.ia; i++) {
          int c = poss[i];
          if (c>=0) vs[c][idxs[c]++] = xs.charAt(i);
        }
        Value[] res = new Value[sz];
        for (int i = 0; i < sz; i++) res[i] = new ChrArr(vs[i]);
        return new HArr(res);
      }
      int[] idxs2 = new int[sz];
      Value[][] vs2 = new Value[sz][];
      for (int i = 0; i < sz; i++) vs2[i] = new Value[rshs[i]];
      for (int i = 0; i < x.ia; i++) {
        int c = poss[i];
        if (c>=0) vs2[c][idxs2[c]++] = x.get(i);
      }
      Value[] res2 = new Value[sz];
      for (int i = 0; i < sz; i++) {
        res2[i] = vs2[i].length>0? Arr.create(vs2[i]) : new EmptyArr(EmptyArr.SHAPE0, x.fItemS());
      }
      return new HArr(res2);
    } else {
      
      int csz = Arr.prod(x.shape, wsz, xsz);
      int[] rsh = new int[wsz];
      for (int i = 0; i < wsz; i++) {
        int sz2 = -1;
        if (max==-1) {
          for (int c : wp[i]) sz2 = Math.max(sz2, c);
          sz2++;
        } else sz2 = max;
        rsh[i] = sz2;
      }
      int sz = Arr.prod(rsh);
      if (sz==0) return new EmptyArr(rsh, new EmptyArr(rsh, x.fItemS()));
      int[][] rshs = new int[sz][]; for (int i = 0; i < rshs.length; i++) rshs[i] = new int[xsz];
      int repl = 1;
      for (int i = wsz-1; i >= 0; i--) {
        int[] ca = new int[rsh[i]];
        int[] cwp = wp[i];
        for (int c : cwp) {
          if (c>=0) ca[c]++;
          else if (c!=-1) throw new DomainError("⊔: didn't expect "+c+" in 𝕨", this);
        }
        int rp = 0;
        while (rp < sz) {
          for (int c : ca) {
            for (int k = 0; k < repl; k++) rshs[rp++][i] = c;
          }
        }
        repl*= rsh[i];
      }
      for (int[] c : rshs) System.arraycopy(x.shape, wsz, c, wsz, xsz-wsz);
      
      MutVal[] vs = new MutVal[sz];
      for (int i = 0; i < sz; i++) vs[i] = new MutVal(rshs[i]);
      recIns(vs, new int[sz], rsh, 0, 0, 0, wp, x, csz);
      
      Value[] res = new Value[sz];
      for (int i = 0; i < sz; i++) res[i] = vs[i].get();
      return new HArr(res, rsh);
    }
  }
  
  private void recIns(MutVal[] vs, int[] ram, int[] rsh, int rp, int k, int ip, int[][] w, Value x, int csz) {
    if (k == rsh.length) {
      vs[rp].copy(x, ip*csz, ram[rp], csz);
      ram[rp]+= csz;
    } else {
      rp*= rsh[k];
      ip*= x.shape[k];
      int[] c = w[k];
      for (int i = 0; i < c.length; i++) {
        if (c[i] >= 0) recIns(vs, ram, rsh, rp+c[i], k+1, ip+i, w, x, csz);
      }
    }
  }
}