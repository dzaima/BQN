package APL.types.functions.builtins.fns;

import APL.Main;
import APL.errors.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.fns2.*;

import java.util.Arrays;

public class OldDownArrowBuiltin extends Builtin {
  @Override public String repr() {
    return "↓";
  }
  
  
  public Value call(Value x) {
    if (x instanceof Primitive) return x;
    if (x.rank <= 1) return new Rank0Arr(x);
    if (x.quickDoubleArr()) {
      double[] dw = x.asDoubleArr();
      int csz = x.shape[x.rank-1]; // chunk size
      int cam = x.ia/csz; // chunk amount
      Value[] res = new Value[cam];
      for (int i = 0; i < cam; i++) {
        double[] c = new double[csz];
        System.arraycopy(dw, i*csz, c, 0, csz);
        // ↑ ≡ for (int j = 0; j < csz; j++) c[j] = dw[i * csz + j];
        res[i] = new DoubleArr(c);
      }
      int[] nsh = new int[x.rank-1];
      System.arraycopy(x.shape, 0, nsh, 0, nsh.length);
      return new HArr(res, nsh);
    }
    int csz = x.shape[x.rank-1]; // chunk size
    int cam = x.ia/csz; // chunk amount
    Value[] res = new Value[cam];
    for (int i = 0; i < cam; i++) {
      Value[] c = new Value[csz];
      for (int j = 0; j < csz; j++) {
        c[j] = x.get(i*csz + j);
      }
      res[i] = Arr.create(c);
    }
    int[] nsh = new int[x.rank-1];
    System.arraycopy(x.shape, 0, nsh, 0, nsh.length);
    return new HArr(res, nsh);
  }
  
  public Value call(Value a, Value w) {
    int[] gsh = a.asIntVec();
    if (gsh.length == 0) return w;
    if (gsh.length > w.rank) throw new DomainError("↓: ≢𝕨 should be less than ≠≢𝕩 ("+gsh.length+" = ≠𝕨; "+Main.formatAPL(w.shape)+" ≡ ≢𝕩)", this);
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
  
  // public Value call(Value a, Value w, DervDimFn dims) {
  //   int[] axV = a.asIntVec();
  //   int[] axK = dims.dims(w.rank);
  //   if (axV.length != axK.length) throw new DomainError("↓: expected 𝕨 and axis specification to have equal number of items (𝕨≡"+Main.formatAPL(axV)+"; axis≡"+dims.format()+")", dims);
  //   int[] sh = w.shape.clone();
  //   int[] off = new int[sh.length];
  //   for (int i = 0; i < axV.length; i++) {
  //     int ax = axK[i];
  //     int am = axV[i];
  //     sh[ax] = w.shape[ax] - Math.abs(am);
  //     if (am > 0) off[ax] = am;
  //   }
  //   return UpArrowBuiltin.on(sh, off, w, this);
  // }
  
  public Value underW(Value o, Value a, Value w) {
    Value v = o instanceof Fun? ((Fun) o).call(call(a, w)) : o;
    int[] ls = a.asIntVec();
    int[] sh = w.shape;
    for (int i = 0; i < ls.length; i++) {
      ls[i] = ls[i]>0? ls[i]-sh[i] : ls[i]+sh[i];
    }
    return UpArrowBuiltin.undo(ls, v, w, this);
  }
  
  public Value under(Value o, Value w) {
    Value v = o instanceof Fun? ((Fun) o).call(call(w)) : o;
    Value[] vs = v.values();
    if (vs.length > 0) {
      int[] sh = vs[0].shape;
      for (int i = 1; i < vs.length; i++) {
        if (!Arrays.equals(vs[i].shape, sh)) throw new LengthError("⌾↓: undoing expected arrays of equal shapes ("+Main.formatAPL(sh)+" ≢ "+Main.formatAPL(vs[i].shape)+")", this, o);
      }
    }
    return GTBuiltin.merge(vs, v.shape, this);
  }
}