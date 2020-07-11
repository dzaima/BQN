package APL.types.functions.builtins.fns;

import APL.Main;
import APL.errors.DomainError;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;

public class TildeBuiltin extends Builtin {
  @Override public String repr() {
    return "~";
  }
  public Value call(Value x) {
    return rec(x);
  }
  
  public Value callInv(Value x) {
    return rec(x);
  }
  
  private Value rec(Value x) {
    if (x instanceof Arr) {
      if (x instanceof BitArr) {
        BitArr wb = (BitArr) x;
        long[] res = new long[wb.arr.length];
        for (int i = 0; i < res.length; i++) res[i] = ~wb.arr[i];
        return new BitArr(res, x.shape);
      }
      
      if (x.quickDoubleArr()) {
        // for (int i = 0; i < w.length; i++) if (w[i] == 0) res[i>>6]|= 1L << (i&63);
        BitArr.BA a = new BitArr.BA(x.shape);
        for (double v : x.asDoubleArr()) a.add(v == 0);
        return a.finish();
      }
      
      Arr o = (Arr) x;
      if (o.ia>0 && o.get(0) instanceof Num) {
        BitArr.BA a = new BitArr.BA(x.ia); // it's probably worth going all-in on creating a bitarr
        for (int i = 0; i < o.ia; i++) {
          Value v = o.get(i);
          if (v instanceof Num) a.add(!Main.bool(v));
          else {
            a = null;
            break;
          }
        }
        if (a != null) return a.finish();
        // could make it reuse the progress made, but ¯\_(ツ)_/¯
      }
      Value[] arr = new Value[o.ia];
      for (int i = 0; i < o.ia; i++) {
        arr[i] = rec(o.get(i));
      }
      return new HArr(arr, o.shape);
    } else if (x instanceof Num) return Main.bool(x)? Num.ZERO : Num.ONE;
    else throw new DomainError("Expected boolean, got "+x.humanType(false), this, x);
  }
  
  public Value call(Value w, Value x) {
    int ia = 0;
    boolean[] leave = new boolean[w.ia];
    a: for (int i = 0; i < w.ia; i++) {
      Value v = w.get(i);
      for (Value c : x) {
        if (v.equals(c)) continue a;
      }
      leave[i] = true;
      ia++;
    }
    Value[] res = new Value[ia];
    int pos = 0;
    for (int i = 0; i < leave.length; i++) {
      if (leave[i]) {
        res[pos++] = w.get(i);
      }
    }
    return Arr.create(res);
  }
}