package APL.types.callable.builtins.fns2;

import APL.errors.RankError;
import APL.tools.MutVal;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.callable.builtins.FnBuiltin;
import APL.types.callable.builtins.mops.CellBuiltin;

public class DownArrowBuiltin extends FnBuiltin {
  public String repr() {
    return "↓";
  }
  
  public Value call(Value x) {
    if (x.rank==0) throw new RankError("↓: argument cannot be scalar", this, x);
    int cells = x.shape[0];
    int csz = CellBuiltin.csz(x);
    Value[] res = new Value[cells+1];
    res[0] = x;
    
    for (int i = 1; i < cells; i++) {
      int am = cells-i;
      int[] sh = x.shape.clone();
      sh[0] = am;
      res[i] = MutVal.cut(x, i*csz, am*csz, sh);
    }
    
    int[] sh0 = x.shape.clone();
    sh0[0] = 0;
    res[cells] = new EmptyArr(sh0, null);
    return new HArr(res);
  }
  
  public Value call(Value w, Value x) {
    int[] gsh = w.asIntVec();
    if (gsh.length == 0) return x;
    int rank = Math.max(x.rank, gsh.length);
    int[] sh = new int[rank];
    System.arraycopy(gsh, 0, sh, 0, gsh.length);
    int rem = rank - gsh.length;
    if (rem > 0) System.arraycopy(x.shape, gsh.length, sh, gsh.length, rem);
    int diff = rank - x.rank;
    int[] off = new int[sh.length];
    for (int i = 0; i < gsh.length; i++) {
      int am = gsh[i];
      int s = i < diff? 1 : x.shape[i - diff];
      sh[i] = s - Math.abs(am);
      if (sh[i] < 0) sh[i] = 0;
      else if (am > 0) off[i] = am;
    }
    return UpArrowBuiltin.on(sh, off, x);
  }
  
  public Value underW(Value o, Value w, Value x) {
    Value v = o instanceof Fun? o.call(call(w, x)) : o;
    int[] ls = w.asIntVec();
    int[] sh = x.shape;
    for (int i = 0; i < ls.length; i++) {
      ls[i] = ls[i]>0? ls[i]-sh[i] : ls[i]+sh[i];
    }
    return UpArrowBuiltin.undo(ls, v, x, this);
  }
}