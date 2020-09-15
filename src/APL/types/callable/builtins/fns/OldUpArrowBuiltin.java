package APL.types.callable.builtins.fns;

import APL.Main;
import APL.errors.DomainError;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.callable.builtins.FnBuiltin;
import APL.types.callable.builtins.fns2.*;

public class OldUpArrowBuiltin extends FnBuiltin {
  @Override public String repr() {
    return "↑";
  }
  
  public Value call(Value x) {
    if (x instanceof Arr) {
      if (x instanceof DoubleArr || x instanceof ChrArr || x instanceof BitArr) return x;
      Value[] subs = x.values();
      return GTBuiltin.merge(subs, x.shape, this);
    } else return x;
  }
  
  
  
  
  
  public Value call(Value w, Value x) {
    int[] gsh = w.asIntVec();
    if (gsh.length == 0) return x;
    if (gsh.length > x.rank) throw new DomainError("↑: ≢𝕨 should be less than ≠≢𝕩 ("+gsh.length+" = ≠𝕨; "+Main.formatAPL(x.shape)+" ≡ ≢𝕩)", this);
    int[] sh = new int[x.rank];
    System.arraycopy(gsh, 0, sh, 0, gsh.length);
    System.arraycopy(x.shape, gsh.length, sh, gsh.length, sh.length - gsh.length);
    int[] off = new int[sh.length];
    for (int i = 0; i < gsh.length; i++) {
      int d = gsh[i];
      if (d < 0) {
        sh[i] = -d;
        off[i] = x.shape[i]-sh[i];
      } else off[i] = 0;
    }
    return UpArrowBuiltin.on(sh, off, x);
  }
  
  // public Value call(Value a, Value w, DervDimFn dims) {
  //   int[] axV = a.asIntVec();
  //   int[] axK = dims.dims(w.rank);
  //   if (axV.length != axK.length) throw new DomainError("↑: expected 𝕨 and axis specification to have equal number of items (𝕨≡"+Main.formatAPL(axV)+"; axis≡"+dims.format()+")", this, dims);
  //   int[] sh = w.shape.clone();
  //   int[] off = new int[sh.length];
  //   for (int i = 0; i < axV.length; i++) {
  //     int ax = axK[i];
  //     int am = axV[i];
  //     sh[ax] = Math.abs(am);
  //     if (am < 0) off[ax] = w.shape[ax] + am;
  //   }
  //   return UpArrowBuiltin.on(sh, off, w, this);
  // }
  
  
  public Value underW(Value o, Value w, Value x) {
    Value v = o instanceof Fun? o.call(call(w, x)) : o;
    return UpArrowBuiltin.undo(w.asIntVec(), v, x, this);
  }
}