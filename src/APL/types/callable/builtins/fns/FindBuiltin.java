package APL.types.callable.builtins.fns;

import APL.errors.*;
import APL.tools.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.callable.builtins.FnBuiltin;
import APL.types.callable.builtins.md1.CellBuiltin;

import java.util.*;

public class FindBuiltin extends FnBuiltin {
  @Override public String repr() {
    return "⍷";
  }
  
  
  public Value call(Value x) {
    if (x.r()==0) throw new DomainError("⍷: argument cannot be a scalar", this, x);
    if (x.ia==0) {
      int[] nsh = x.shape.clone();
      nsh[0] = Math.min(nsh[0], 1);
      return new EmptyArr(nsh, Num.ZERO);
    }
    if (x.r() > 1) {
      Value[] rcs = call(new HArr(CellBuiltin.cells(x))).values();
      return GTBuiltin.merge(rcs, new int[]{rcs.length}, this);
    }
    if (x.quickIntArr()) {
      HashSet<Integer> vals = new HashSet<>();
      MutIntArr res = new MutIntArr(10);
      for (int c : x.asIntArr()) if (vals.add(c)) res.add(c);
      return res.get();
    }
    HashSet<Value> vals = new HashSet<>();
    ArrayList<Value> res = new ArrayList<>();
    for (Value c : x) if (vals.add(c)) res.add(c);
    return Arr.create(res);
  }
  
  public Value call(Value w, Value x) {
    if (w.r() != x.r()) throw new RankError("⍷: argument ranks should be equal ("+w.r()+" ≠ "+x.r()+")", this, x);
    BitArr.BC res = new BitArr.BC(x.shape);
    if (w.r() == 1) {
      if (w instanceof BitArr && x instanceof BitArr) {
        long[] al = ((BitArr) w).arr;
        long[] wl = ((BitArr) x).arr;
        w: for (int ir = 0; ir < x.ia-w.ia+1; ir++) {
          for (int ia = 0; ia < w.ia; ia++) {
            int iw = ia + ir;
            long la = al[ia>>6] >> (ia & 63);
            long lw = wl[iw>>6] >> (iw & 63);
            if ((la&1) != (lw&1)) continue w;
          }
          res.set(ir);
        }
      } else if (w.quickDoubleArr() && x.quickDoubleArr()) {
        double[] wd = w.asDoubleArr();
        double[] xd = x.asDoubleArr();
        w: for (int ir = 0; ir < x.ia-w.ia+1; ir++) {
          for (int ia = 0; ia < w.ia; ia++) {
            if (wd[ia] != xd[ia + ir]) continue w;
          }
          res.set(ir);
        }
      } else {
        w: for (int ir = 0; ir < x.ia-w.ia+1; ir++) {
          for (int ia = 0; ia < w.ia; ia++) {
            if (!w.get(ia).eq(x.get(ia + ir))) continue w;
          }
          res.set(ir);
        }
      }
    } else {
      Indexer ind = new Indexer(Indexer.add(Indexer.sub(x.shape, w.shape), 1));
      w: for (int[] inW : ind) {
        for (int[] inA : new Indexer(w.shape)) {
          Value vA = w.simpleAt(inA);
          Value vW = x.simpleAt(Indexer.add(inA, inW));
          if (!vA.eq(vW)) continue w;
        }
        res.set(Indexer.fromShape(x.shape, inW));
      }
    }
    return res.finish();
  }
}