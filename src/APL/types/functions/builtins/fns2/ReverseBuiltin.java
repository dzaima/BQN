package APL.types.functions.builtins.fns2;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;

public class ReverseBuiltin extends Builtin {
  @Override public String repr() {
    return "⌽";
  }
  
  
  public Value call(Value x) {
    return on(x);
  }
  public static Value on(Value w) {
    if (w instanceof Primitive) return w;
    return ((Arr) w).reverseOn(w.rank-1);
  }
  public Value callInv(Value w) {
    return call(w);
  }
  
  
  public Value call(Value a, Value w) { // valuecopy
    if (a instanceof Primitive) return on(a.asInt(), w);
    throw new DomainError("⌽: 𝕨 must be a scalar number", this, w);
  }
  
  @Override public Value callInvW(Value a, Value w) {
    return call(numM(MinusBuiltin.NF, a), w);
  }
  
  
  
  public static Value on(int a, Value w) {
    if (w.ia==0) return w;
    if (a == 0) return w;
    a = Math.floorMod(a, w.shape[0]);
    int csz = Arr.prod(w.shape, 1, w.shape.length);
    int pA = csz*a; // first part
    int pB = w.ia - pA; // second part
    if (w instanceof BitArr) {
      BitArr wb = (BitArr) w;
      BitArr.BA c = new BitArr.BA(wb.shape);
      c.add(wb, a, wb.ia);
      c.add(wb, 0, a);
      return c.finish();
    } else if (w.quickDoubleArr()) {
      double[] vs = w.asDoubleArr();
      double[] res = new double[w.ia];
      System.arraycopy(vs,  0, res, pB, pA);
      System.arraycopy(vs, pA, res,  0, pB);
      return new DoubleArr(res, w.shape);
    } else {
      Value[] vs = w.values();
      Value[] res = new Value[w.ia];
      System.arraycopy(vs,  0, res, pB, pA);
      System.arraycopy(vs, pA, res,  0, pB);
      return Arr.create(res, w.shape);
    }
  }
}