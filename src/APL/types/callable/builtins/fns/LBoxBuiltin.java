package APL.types.callable.builtins.fns;

import APL.Main;
import APL.errors.*;
import APL.tools.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.callable.builtins.FnBuiltin;
import APL.types.callable.builtins.md1.CellBuiltin;

import java.util.Arrays;

public class LBoxBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "⊏"; }
  
  public Value call(Value x) {
    if (x.r()==0) throw new RankError("⊏: scalar argument isn't allowed", this);
    if (x.shape[0]==0) throw new LengthError("⊏: argument shape cannot start with 0 (had shape "+Main.formatAPL(x.shape)+")", this);
    int[] nsh = new int[x.r()-1];
    System.arraycopy(x.shape, 1, nsh, 0, nsh.length);
    return MutVal.cut(x, 0, Arr.prod(nsh), nsh);
  }
  
  public Value call(Value w, Value x) {
    if (x.r()==0) throw new RankError("⊏: scalar 𝕩 isn't allowed", this);
    if (w instanceof Num) return getCell(w.asInt(), x, this);
    
    int wr = w.r();
    int xr = x.r();
    if (w.ia==0 || w.quickDepth1() || w.first() instanceof Num) {
      int[] sh = new int[wr+xr-1];
      System.arraycopy(w.shape, 0, sh, 0, wr);
      System.arraycopy(x.shape, 1, sh, wr, xr-1);
      int[] wi = w.asIntArr();
      spec: if (w.r()==1 && x.r()==1) {
        if (x.quickDoubleArr()) {
          if (x.quickIntArr()) {
            if (x instanceof BitArr) {
              long[] xl = ((BitArr) x).arr;
              int xa = x.ia;
              long[] res = new long[BitArr.sizeof(wi.length)];
              for (int i = 0; i < wi.length; i++) {
                int c = wi[i];
                if (c<0) c+= xa;
                if (c<0 || c>=xa) break spec;
                res[i>>6]|= (xl[c>>6]>>(c&63) & 1) << (i&63);
              }
              return new BitArr(res, sh);
            }
            int[] xi = x.asIntArr();
            int[] res = new int[wi.length];
            for (int i = 0; i < wi.length; i++) {
              int c = wi[i];
              if (c<0) c+= xi.length;
              if (c<0 || c>=xi.length) break spec;
              res[i] = xi[c];
            }
            return new IntArr(res, sh);
          }
          double[] xd = x.asDoubleArr();
          double[] res = new double[wi.length];
          for (int i = 0; i < wi.length; i++) {
            int c = wi[i];
            if (c<0) c+= xd.length;
            if (c<0 || c>=xd.length) break spec;
            res[i] = xd[c];
          }
          return new DoubleArr(res, sh);
        }
        if (x instanceof ChrArr) {
          String xs = ((ChrArr) x).s;
          char[] res = new char[wi.length];
          for (int i = 0; i < wi.length; i++) {
            int c = wi[i];
            if (c<0) c+= xs.length();
            if (c<0 || c>=xs.length()) break spec;
            res[i] = xs.charAt(c);
          }
          return new ChrArr(res, sh);
        }
        Value[] xv = x.values();
        Value[] res = new Value[wi.length];
        for (int i = 0; i < wi.length; i++) {
          int c = wi[i];
          if (c<0) c+= xv.length;
          if (c<0 || c>=xv.length) break spec;
          res[i] = xv[c];
        }
        return Arr.create(res, sh);
      }
      MutVal res = new MutVal(sh);
      int csz = CellBuiltin.csz(x);
      for (int i = 0; i < wi.length; i++) res.copy(getCell(wi[i], x, this), 0, csz*i, csz);
      return res.get();
    } else {
      if (wr > 1) throw new RankError("⊏: depth 2 𝕨 must be of rank 0 or 1 (shape ≡ "+Main.formatAPL(w.shape)+")", this);
      
      int shl = 0;
      Value[] av = w.values();
      for (Value c : av) shl+= c.r();
      int[] sh = new int[shl + xr-w.ia];
      System.arraycopy(x.shape, w.ia, sh, shl, xr-w.ia);
      
      int cp = 0;
      for (Value c : av) {
        System.arraycopy(c.shape, 0, sh, cp, c.r());
        cp+= c.r();
      }
      int[] c = new int[w.ia];
      int csz = 1;
      for (int i = shl; i < sh.length; i++) csz*= sh[i];
      
      MutVal res = new MutVal(sh, x);
      cellRec(res, c, 0, w, x, csz, 0);
      return res.get();
    }
  }
  
  private int cellRec(MutVal res, int[] c, int i, Value w, Value x, int csz, int rp) {
    if (i==c.length) {
      int ip = 0;
      for (int j = 0; j < c.length; j++) { // +todo not
        int a = x.shape[j];
        int o = c[j];
        ip*= a;
        ip+= Indexer.scal(o, a, this);
      }
      ip*= csz;
      res.copy(x, ip, rp, csz);
      rp+= csz;
    } else {
      for (int d : w.get(i).asIntArr()) {
        c[i] = d;
        rp = cellRec(res, c, i+1, w, x, csz, rp);
      }
    }
    return rp;
  }
  
  public Value underW(Value o, Value w, Value x) {
    Value call = call(w, x);
    Value v = o instanceof Fun? o.call(call) : o;
    if (MatchBuiltin.full(w) > 1) throw new NYIError("⌾⊏ 1<≠≢𝕨", this);
    if (!Arrays.equals(call.shape, v.shape)) throw new DomainError("F⌾⊏: F didn't return equal shape array (was "+Main.formatAPL(call.shape)+", got "+Main.formatAPL(v.shape)+")", this);
    int[] is = w.asIntArr();
    if (x.quickIntArr() && v.quickIntArr()) {
      int[] res = x.asIntArrClone(); int[] vi = v.asIntArr();
      for (int i = 0; i < is.length; i++) res[is[i]] = vi[i];
      return new IntArr(res, x.shape);
    }
    Value[] res = x.valuesClone();
    for (int i = 0; i < is.length; i++) res[is[i]] = v.get(i);
    return Arr.create(res, x.shape);
  }
  public Value under(Value o, Value x) {
    Value call = call(x);
    Value v = o instanceof Fun? o.call(call) : o;
    MutVal m = new MutVal(x.shape, x, x.ia);
    if (!Arrays.equals(call.shape, v.shape)) throw new DomainError("F⌾⊏: F didn't return equal shape array (was "+Main.formatAPL(call.shape)+", got "+Main.formatAPL(v.shape)+")", this);
    m.copy(v, 0, 0, call.ia);
    m.copy(x, call.ia, call.ia, x.ia-call.ia);
    return m.get();
  }
  
  public static Value getCell(int a, Value x, Callable blame) { // expects non-scalar x
    int cam = x.shape[0];        // cell amount
    int csz = CellBuiltin.csz(x);// cell size
    int start = csz*Indexer.scal(a, cam, blame);
    
    int[] sh = new int[x.r()-1];
    System.arraycopy(x.shape, 1, sh, 0, sh.length);
    return MutVal.cut(x, start, csz, sh);
  }
}