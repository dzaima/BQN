package APL.types.functions.builtins.fns2;

import APL.Main;
import APL.errors.DomainError;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;

public class DownArrowBuiltin extends Builtin {
  public String repr() {
    return "↓";
  }
  
  public Value call(Value w) { // TODO scalars? opt for nums?
    Value[] vs = w.values();
    int cells = w.shape[0];
    int csz = w.ia/cells;
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
    if (gsh.length > w.rank) throw new DomainError("↓: ≢⍺ should be less than ⍴⍴⍵ ("+gsh.length+" = ≢⍺; "+ Main.formatAPL(w.shape)+" ≡ ⍴⍵)", this);
    int[] sh = new int[w.rank];
    System.arraycopy(gsh, 0, sh, 0, gsh.length);
    System.arraycopy(w.shape, gsh.length, sh, gsh.length, sh.length - gsh.length);
    int[] off = new int[sh.length];
    for (int i = 0; i < gsh.length; i++) {
      int am = gsh[i];
      sh[i] = w.shape[i] - Math.abs(am);
      if (am > 0) off[i] = am;
    }
    return UpArrowBuiltin.on(sh, off, w, this);
  }
  
  public Value underW(Value o, Value a, Value w) {
    Value v = o instanceof Fun? ((Fun) o).call(call(a, w)) : (Value) o;
    int[] ls = a.asIntVec();
    int[] sh = w.shape;
    for (int i = 0; i < ls.length; i++) {
      ls[i] = ls[i]>0? ls[i]-sh[i] : ls[i]+sh[i];
    }
    return UpArrowBuiltin.undo(ls, v, w, this);
  }
}
