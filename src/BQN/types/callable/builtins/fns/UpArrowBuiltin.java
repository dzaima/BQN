package BQN.types.callable.builtins.fns;

import BQN.Main;
import BQN.errors.*;
import BQN.tools.*;
import BQN.types.*;
import BQN.types.arrs.*;
import BQN.types.callable.builtins.FnBuiltin;
import BQN.types.callable.builtins.md1.CellBuiltin;

public class UpArrowBuiltin extends FnBuiltin {
  
  public String ln(FmtInfo f) { return "↑"; }
  
  public Value call(Value x) {
    if (x.r()==0) throw new RankError("↑: argument cannot be scalar", this);
    int cells = x.shape[0];
    int csz = CellBuiltin.csz(x);
    Value[] res = new Value[cells+1];
    int[] sh0 = x.shape.clone();
    sh0[0] = 0;
    res[0] = new EmptyArr(sh0, null);
    for (int i = 1; i < cells; i++) {
      int[] sh = x.shape.clone();
      sh[0] = i;
      res[i] = MutVal.cut(x, 0, i*csz, sh);
    }
    res[cells] = x;
    return new HArr(res);
  }
  
  
  
  public Value call(Value w, Value x) {
    int[] gsh = w.asIntVec();
    if (gsh.length == 0) return x;
    int rank = Math.max(x.r(), gsh.length);
    int[] sh = new int[rank];
    System.arraycopy(gsh, 0, sh, 0, gsh.length);
    int rem = rank - gsh.length;
    if (rem > 0) System.arraycopy(x.shape, gsh.length, sh, gsh.length, rem);
    int diff = rank - x.r();
    boolean overtake = false;
    int[] off = new int[rank];
    for (int i = 0; i < gsh.length; i++) {
      int d = sh[i];
      int s = i<diff? 1 : x.shape[i-diff];
      if (d < 0) {
        off[i] = s+sh[i];
        sh[i] = -d;
        if (-d > s) overtake = true;
      } else if (d > s) overtake = true;
    }
    if (overtake) {
      Value proto = x.fItem();
      MutVal res = new MutVal(sh);
      if (x.r()<=1 && gsh.length==1) {
        if (off[0]==0) {
          res.copy(x, 0, 0, x.ia);
          res.fill(proto, x.ia, res.ia);
        } else {
          res.copy(x, 0, res.ia-x.ia, x.ia);
          res.fill(proto, 0, res.ia-x.ia);
        }
        return res.get();
      }
      int l = sh.length;
      int rp = 0;
      int[] xsh = x.shape;
      idx: for (int[] c : new Indexer(sh)) {
        int ip = 0;
        for (int i = 0; i < l; i++) {
          int ri = c.length-i-1;
          int cp = c[i]+off[i];
          int xl = ri<xsh.length? xsh[xsh.length-ri-1] : 1;
          if (cp>=xl || cp<0) {
            res.set(rp++, proto);
            continue idx;
          }
          ip = ip*xl+cp;
        }
        res.set(rp++, x.get(ip));
      }
      return res.get();
    }
    return on(sh, off, x);
  }
  
  public static Value on(int[] sh, int[] off, Value x) { // valuecopy
    int rank = sh.length;
    assert rank==off.length && rank>=x.r();
    for (int j : sh) if (j == 0) return new EmptyArr(sh, x.fItemS());
    if (rank > x.r()) {
      int d = rank - x.r();
      int[] ssh  = new int[x.r()]; System.arraycopy(sh , d, ssh , 0, x.r());
      int[] soff = new int[x.r()]; System.arraycopy(off, d, soff, 0, x.r());
      return on(ssh, soff, x).ofShape(sh);
    }
    if (rank == 1) {
      int s = off[0];
      int l = sh[0];
      if (x instanceof BitArr && s==0) { // todo this might be pointless later
        BitArr xb = (BitArr) x;
        long[] ls = new long[BitArr.sizeof(l)];
        System.arraycopy(xb.arr, 0, ls, 0, ls.length);
        return new BitArr(ls, Arr.vecsh(l));
      }
      
      return MutVal.cut(x, s, l, new int[]{l});
    }
    int ia = Arr.prod(sh);
    if (x instanceof ChrArr) {
      char[] arr = new char[ia];
      String s = ((ChrArr) x).s;
      int i = 0;
      for (int[] index : new Indexer(sh, off)) {
        arr[i] = s.charAt(Indexer.fromShape(x.shape, index));
        i++;
      }
      return new ChrArr(arr, sh);
    }
    if (x.quickIntArr()) {
      int[] arr = new int[ia];
      int[] wd = x.asIntArr();
      int i = 0;
      for (int[] index : new Indexer(sh, off)) {
        arr[i] = wd[Indexer.fromShape(x.shape, index)];
        i++;
      }
      return new IntArr(arr, sh);
    }
    if (x.quickDoubleArr()) {
      double[] arr = new double[ia];
      double[] wd = x.asDoubleArr();
      int i = 0;
      for (int[] index : new Indexer(sh, off)) {
        arr[i] = wd[Indexer.fromShape(x.shape, index)];
        i++;
      }
      return new DoubleArr(arr, sh);
    }
    Value[] arr2 = new Value[ia];
    int i2 = 0;
    for (int[] index : new Indexer(sh, off)) {
      arr2[i2] = x.at(index);
      i2++;
    }
    return Arr.create(arr2, sh);
  }
  
  
  
  
  
  public Value underW(Value o, Value w, Value x) {
    Value v = o instanceof Fun? o.call(call(w, x)) : o;
    return undo(w.asIntVec(), v, x, this);
  }
  
  public static Value undo(int[] e, Value w, Value origW, Callable blame) {
    if (e.length==1 && w.r()==1) {
      int am = e[0];
      if (am > 0) return JoinBuiltin.on(w, on(new int[]{origW.ia-am}, e, origW), blame);
      else return JoinBuiltin.on(on(new int[]{origW.ia+am}, new int[]{0}, origW), w, blame);
    }
    chk: {
      fail: if (w.r() == e.length) {
        for (int i = 0; i < e.length; i++) {
          if (Math.abs(e[i]) != w.shape[i]) break fail;
        }
        break chk;
      }
      throw new LengthError("x⌾(N↓): x didn't match expected shape ("+Main.fArr(w.shape)+" ≡ ≢x; expected "+Main.fArr(e)+")", blame);
    }
    Value[] r = new Value[origW.ia];
    int[] s = origW.shape;
    Indexer idx = new Indexer(s);
    int[] tmp = new int[e.length];
    for (int[] i : idx) {
      Value c;
      boolean in = true;
      for (int j = 0; j < e.length; j++) {
        int ep = e[j];
        int ip = i[j];
        int lp = s[j];
        if (ep<0? ip <= lp+ep-1 : ip >= ep) {
          in = false;
          break;
        }
      }
      if (in) {
        for (int j = 0; j < e.length; j++) {
          tmp[j] = e[j]<0? i[j]-e[j]-s[j]: i[j];
        }
        c = w.simpleAt(tmp);
      } else {
        c = origW.simpleAt(i);
      }
      r[idx.pos()] = c;
      
    }
    
    return Arr.create(r, s);
  }
  
  public Value callInv(Value x) {
    if (x.ia==0) throw new DomainError("↑⁼: Argument had 0 items", this);
    Value r = x.get(x.ia-1);
    if (!call(r).eq(x)) throw new DomainError("↑⁼: Argument isn't a prefix array", this);
    return r;
  }
}