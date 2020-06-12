package APL.types.functions.builtins.fns2;

import APL.Main;
import APL.errors.DomainError;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.fns.OldUpArrowBuiltin;

public class UpArrowBuiltin extends Builtin {
  public String repr() {
    return "↑";
  }
  
  public Value call(Value w) { // TODO scalars? opt for nums?
    Value[] vs = w.values();
    int cells = w.shape[0];
    int csz = w.ia/cells;
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
    if (gsh.length > w.rank) throw new DomainError("↑: ≢⍺ should be less than ⍴⍴⍵ ("+gsh.length+" = ≢⍺; "+ Main.formatAPL(w.shape)+" ≡ ⍴⍵)", this);
    int[] sh = new int[w.rank];
    System.arraycopy(gsh, 0, sh, 0, gsh.length);
    System.arraycopy(w.shape, gsh.length, sh, gsh.length, sh.length - gsh.length);
    int[] off = new int[sh.length];
    for (int i = 0; i < gsh.length; i++) {
      int d = gsh[i];
      if (d < 0) {
        sh[i] = -d;
        off[i] = w.shape[i]-sh[i];
      } else off[i] = 0;
    }
    return OldUpArrowBuiltin.on(sh, off, w, this);
  }
  
  public Value underW(Obj o, Value a, Value w) {
    Value v = o instanceof Fun? ((Fun) o).call(call(a, w)) : (Value) o;
    return OldUpArrowBuiltin.undo(a.asIntVec(), v, w, this);
  }
}
