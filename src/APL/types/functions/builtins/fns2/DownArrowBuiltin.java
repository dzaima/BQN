package APL.types.functions.builtins.fns2;

import APL.errors.RankError;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.mops.CellBuiltin;

public class DownArrowBuiltin extends Builtin {
  public String repr() {
    return "↓";
  }
  
  public Value call(Value w) { // TODO scalars? opt for nums?
    if (w.rank==0) throw new RankError("↑: argument cannot be scalar", this, w);
    Value[] vs = w.values();
    int cells = w.shape[0];
    int csz = CellBuiltin.csz(w);
    Value[] res = new Value[cells+1];
  
    res[0] = w;
    
    for (int i = 1; i < cells; i++) {
      int am = cells-i;
      Value[] c = new Value[am*csz];
      System.arraycopy(vs, i*csz, c, 0, c.length);
      int[] sh = w.shape.clone();
      sh[0] = am;
      res[i] = Arr.create(c, sh);
    }
  
    int[] sh0 = w.shape.clone();
    sh0[0] = 0;
    res[cells] = new EmptyArr(sh0, null);
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
    int[] off = new int[sh.length];
    for (int i = 0; i < gsh.length; i++) {
      int am = gsh[i];
      int s = i < diff ? 1 : w.shape[i - diff];
      sh[i] = s - Math.abs(am);
      if (sh[i] < 0) sh[i] = 0;
      else if (am > 0) off[i] = am;
    }
    return UpArrowBuiltin.on(sh, off, w, this);
  }
  
  public Value underW(Value o, Value a, Value w) {
    Value v = o instanceof Fun? ((Fun) o).call(call(a, w)) : o;
    int[] ls = a.asIntVec();
    int[] sh = w.shape;
    for (int i = 0; i < ls.length; i++) {
      ls[i] = ls[i]>0? ls[i]-sh[i] : ls[i]+sh[i];
    }
    return UpArrowBuiltin.undo(ls, v, w, this);
  }
}
