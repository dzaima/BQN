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
  public static Value on(Value x) {
    if (x instanceof Primitive) return x;
    return ((Arr) x).reverseOn(0);
  }
  public Value callInv(Value x) {
    return call(x);
  }
  
  
  public Value call(Value w, Value x) {
    if (w instanceof Primitive) return on(w.asInt(), x);
    throw new DomainError("⌽: 𝕨 must be a scalar number", this, x);
  }
  
  @Override public Value callInvW(Value w, Value x) {
    return call(numM(MinusBuiltin.NF, w), x);
  }
  
  
  
  public static Value on(int a, Value x) {
    if (x.ia==0) return x;
    if (a == 0) return x;
    a = Math.floorMod(a, x.shape[0]);
    int csz = Arr.prod(x.shape, 1, x.shape.length);
    int pA = csz*a; // first part
    int pB = x.ia - pA; // second part
    if (x instanceof BitArr) {
      BitArr wb = (BitArr) x;
      BitArr.BA c = new BitArr.BA(wb.shape);
      c.add(wb, a, wb.ia);
      c.add(wb, 0, a);
      return c.finish();
    } else if (x.quickDoubleArr()) {
      double[] vs = x.asDoubleArr();
      double[] res = new double[x.ia];
      System.arraycopy(vs,  0, res, pB, pA);
      System.arraycopy(vs, pA, res,  0, pB);
      return new DoubleArr(res, x.shape);
    } else {
      Value[] vs = x.values();
      Value[] res = new Value[x.ia];
      System.arraycopy(vs,  0, res, pB, pA);
      System.arraycopy(vs, pA, res,  0, pB);
      return Arr.create(res, x.shape);
    }
  }
}