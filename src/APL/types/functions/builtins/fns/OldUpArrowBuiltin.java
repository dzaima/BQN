package APL.types.functions.builtins.fns;

import APL.*;
import APL.errors.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.dimensions.*;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.fns2.*;

public class OldUpArrowBuiltin extends Builtin implements DimDFn {
  @Override public String repr() {
    return "↑";
  }
  
  public Value call(Value w) {
    if (w instanceof Arr) {
      if (w instanceof DoubleArr || w instanceof ChrArr || w instanceof BitArr) return w;
      Value[] subs = w.values();
      return GTBuiltin.merge(subs, w.shape, this);
    } else return w;
  }
  
  
  
  
  
  public Value call(Value a, Value w) {
    int[] gsh = a.asIntVec();
    if (gsh.length == 0) return w;
    if (gsh.length > w.rank) throw new DomainError("↑: ≢⍺ should be less than ⍴⍴⍵ ("+gsh.length+" = ≢⍺; "+Main.formatAPL(w.shape)+" ≡ ⍴⍵)", this);
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
    return UpArrowBuiltin.on(sh, off, w, this);
  }
  
  public Value call(Value a, Value w, DervDimFn dims) {
    int[] axV = a.asIntVec();
    int[] axK = dims.dims(w.rank);
    if (axV.length != axK.length) throw new DomainError("↑: expected ⍺ and axis specification to have equal number of items (⍺≡"+Main.formatAPL(axV)+"; axis≡"+dims.format()+")", this, dims);
    int[] sh = w.shape.clone();
    int[] off = new int[sh.length];
    for (int i = 0; i < axV.length; i++) {
      int ax = axK[i];
      int am = axV[i];
      sh[ax] = Math.abs(am);
      if (am < 0) off[ax] = w.shape[ax] + am;
    }
    return UpArrowBuiltin.on(sh, off, w, this);
  }
  
  
  public Value underW(Obj o, Value a, Value w) {
    Value v = o instanceof Fun? ((Fun) o).call(call(a, w)) : (Value) o;
    return UpArrowBuiltin.undo(a.asIntVec(), v, w, this);
  }
}