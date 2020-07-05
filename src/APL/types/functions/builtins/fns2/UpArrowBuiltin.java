package APL.types.functions.builtins.fns2;

import APL.*;
import APL.errors.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.mops.CellBuiltin;

public class UpArrowBuiltin extends Builtin {
  
  public String repr() {
    return "↑";
  }
  
  public Value call(Value w) { // TODO scalars? opt for nums?
    if (w.rank==0) throw new RankError("↑: argument cannot be scalar", this, w);
    Value[] vs = w.values();
    int cells = w.shape[0];
    int csz = CellBuiltin.csz(w);
    Value[] res = new Value[cells+1];
    int[] sh0 = w.shape.clone();
    sh0[0] = 0;
    res[0] = new EmptyArr(sh0, null);
    for (int i = 1; i < cells; i++) {
      Value[] c = new Value[i*csz];
      System.arraycopy(vs, 0, c, 0, c.length);
      int[] sh = w.shape.clone();
      sh[0] = i;
      res[i] = Arr.create(c, sh);
    }
    res[cells] = w;
    return new HArr(res);
  }
  
  
  
  public Value call(Value a, Value w) {
    int[] gsh = a.asIntVec();
    if (gsh.length == 0) return w;
    int rank = Math.max(w.rank, gsh.length);
    int[] sh = new int[rank];
    System.arraycopy(gsh, 0, sh, 0, gsh.length);
    int rem = rank - gsh.length;
    if (rem > 0) System.arraycopy(w.shape, gsh.length, sh, gsh.length, rem);
    int diff = rank - w.rank;
    boolean proto = false;
    int[] off = new int[rank];
    for (int i = 0; i < gsh.length; i++) {
      int d = gsh[i];
      int s = i < diff ? 1 : w.shape[i - diff];
      if (d > s) proto = true;
      if (d < 0) {
        sh[i] = -d;
        off[i] = s-sh[i];
      } else off[i] = 0;
    }
    if (proto) {
      throw new NYIError("No overtake yet", this);
    }
    return on(sh, off, w, this);
  }
  
  public static Value on(int[] sh, int[] off, Value w, Callable blame) {
    int rank = sh.length;
    assert rank==off.length && rank>=w.rank;
    if (rank > w.rank) {
      boolean empty = false; // has to be empty or all leading 1s
      int d = rank - w.rank;
      for (int i = 0; i < d; i++) {
        if (sh[i] == 0) { empty = true; break; }
      }
      if (empty) {
        return new EmptyArr(sh, w.safePrototype());
      } else {
        int[] ssh  = new int[w.rank]; System.arraycopy(sh , d, ssh , 0, w.rank);
        int[] soff = new int[w.rank]; System.arraycopy(off, d, soff, 0, w.rank);
        return on(ssh, soff, w, blame).ofShape(sh);
      }
    }
    if (rank == 1) {
      int s = off[0];
      int l = sh[0];
      if (w instanceof BitArr) {
        BitArr wb = (BitArr) w;
        if (s == 0) {
          long[] ls = new long[BitArr.sizeof(l)];
          System.arraycopy(wb.arr, 0, ls, 0, ls.length);
          return new BitArr(ls, new int[]{l});
        } else {
          BitArr.BA res = new BitArr.BA(l);
          res.add(wb, s, w.ia);
          return res.finish();
        }
      }
      if (w instanceof ChrArr) {
        char[] res = new char[l];
        String ws = ((ChrArr) w).s;
        ws.getChars(s, s+l, res, 0); // ≡ for (int i = 0; i < l; i++) res[i] = ws.charAt(s+i);
        return new ChrArr(res);
      }
      if (w.quickDoubleArr()) {
        double[] res = new double[l];
        double[] wd = w.asDoubleArr();
        System.arraycopy(wd, s, res, 0, l); // ≡ for (int i = 0; i < l; i++) res[i] = wd[s+i];
        return new DoubleArr(res);
      }
      
      Value[] res = new Value[l];
      for (int i = 0; i < l; i++) res[i] = w.get(s+i);
      return Arr.create(res);
    }
    int ia = Arr.prod(sh);
    if (w instanceof ChrArr) {
      char[] arr = new char[ia];
      String s = ((ChrArr) w).s;
      int i = 0;
      for (int[] index : new Indexer(sh, off)) {
        arr[i] = s.charAt(Indexer.fromShape(w.shape, index));
        i++;
      }
      return new ChrArr(arr, sh);
    }
    if (w.quickDoubleArr()) {
      double[] arr = new double[ia];
      double[] wd = w.asDoubleArr();
      int i = 0;
      for (int[] index : new Indexer(sh, off)) {
        arr[i] = wd[Indexer.fromShape(w.shape, index)];
        i++;
      }
      return new DoubleArr(arr, sh);
    }
    Value[] arr = new Value[ia];
    int i = 0;
    for (int[] index : new Indexer(sh, off)) {
      arr[i] = w.at(index);
      i++;
    }
    return Arr.create(arr, sh);
  }
  
  
  
  
  
  public Value underW(Value o, Value a, Value w) {
    Value v = o instanceof Fun? ((Fun) o).call(call(a, w)) : o;
    return undo(a.asIntVec(), v, w, this);
  }
  
  public static Value undo(int[] e, Value w, Value origW, Callable blame) {
    if (e.length==1 && w.rank==1) {
      int am = e[0];
      if (am > 0) return JoinBuiltin.cat(w, on(new int[]{origW.ia-am}, e, origW, blame), 0, blame);
      else return JoinBuiltin.cat(on(new int[]{origW.ia+am}, new int[]{0}, origW, blame), w, 0, blame);
    }
    chk: {
      fail: if (w.rank == e.length) {
        for (int i = 0; i < e.length; i++) {
          if (Math.abs(e[i]) != w.shape[i]) break fail;
        }
        break chk;
      }
      throw new LengthError("x⌾(N↓): x didn't match expected shape ("+Main.formatAPL(w.shape)+" ≡ ⍴x; expected "+Main.formatAPL(e)+")", blame);
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
}
