package APL.types.functions.builtins.fns;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;

import java.util.ArrayList;


public class EpsilonBuiltin extends Builtin {
  @Override public String repr() {
    return "∊";
  }
  
  
  
  public Value call(Value x) {
    var res = new ArrayList<Value>();
    rec(res, x);
    return Arr.create(res);
  }
  
  private void rec(ArrayList<Value> arr, Value v) {
    if (v instanceof Primitive) {
      arr.add(v);
    } else {
      if (v instanceof BitArr) {
        BitArr ba = (BitArr) v;
        for (int i = 0; i < ba.ia; i++) arr.add(ba.get(i));
      } else if (v.quickDoubleArr()) {
        for (double d : v.asDoubleArr()) arr.add(Num.of(d));
      } else if (v instanceof ChrArr) {
        String s = ((ChrArr) v).s;
        for (int i = 0; i < s.length(); i++) {
          arr.add(Char.of(s.charAt(i)));
        }
      } else for (Value c : v) rec(arr, c);
    }
  }
  
  public Value call(Value w, Value x) {
    if (w.scalar()) {
      Value a1 = w.first();
      for (Value v : x) {
        if (v.equals(a1)) {
          return Num.ONE;
        }
      }
      return Num.ZERO;
    }
    BitArr.BA ba = new BitArr.BA(w.shape);
    for (int i = 0; i < w.ia; i++) {
      Value av = w.get(i);
      boolean b = false;
      for (Value v : x) {
        if (v.equals(av)) {
          b = true;
          break;
        }
      }
      ba.add(b);
    }
    return ba.finish();
  }
  
  
  public Value under(Value o, Value x) {
    Value v = o instanceof Fun? ((Fun) o).call(call(x)) : o;
    Value[] vs = v.values();
    Value[] res = new Value[x.ia];
    int e = copyIn(res, vs, x, 0);
    if (e != v.ia) throw new DomainError("⌾∊ expected equal amount of output & output items", this);
    return Arr.create(res, x.shape);
  }
  private int copyIn(Value[] res, Value[] vs, Value orig, int s) {
    for (int i = 0; i < orig.ia; i++) {
      Value origN = orig.get(i);
      if (origN instanceof Primitive) {
        res[i] = vs[s++];
      } else {
        Value[] resN = new Value[origN.ia];
        s = copyIn(resN, vs, origN, s);
        res[i] = Arr.create(resN, origN.shape);
      }
    }
    return s;
  }
}