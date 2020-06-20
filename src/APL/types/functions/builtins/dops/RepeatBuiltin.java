package APL.types.functions.builtins.dops;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.functions.*;

public class RepeatBuiltin extends Dop {
  @Override public String repr() {
    return "⍟";
  }
  
  public Value call(Value aa, Value ww, Value w, DerivedDop derv) {
    Fun aaf = aa.asFun();
    int am = ww.asFun().call(w).asInt();
    if (am < 0) {
      for (int i = 0; i < -am; i++) {
        w = aaf.callInv(w);
      }
    } else for (int i = 0; i < am; i++) {
      w = aaf.call(w);
    }
    return w;
  }
  
  public Value callInv(Value aa, Value ww, Value w) {
    Fun aaf = isFn(aa, '⍶');
    if (ww instanceof Fun) throw new DomainError("(f⍣g)A cannot be inverted", this);
    
    int am = ww.asInt();
    if (am < 0) {
      for (int i = 0; i < -am; i++) {
        w = aaf.call(w);
      }
    } else for (int i = 0; i < am; i++) {
      w = aaf.callInv(w);
    }
    return w;
  }
  
  public Value call(Value aa, Value ww, Value a, Value w, DerivedDop derv) {
    Fun aaf = aa.asFun();
    int am = ww.asFun().call(a, w).asInt();
    if (am < 0) {
      for (int i = 0; i < -am; i++) {
        w = aaf.callInvW(a, w);
      }
    } else for (int i = 0; i < am; i++) {
      w = aaf.call(a, w);
    }
    return w;
  }
  
  public Value callInvW(Value aa, Value ww, Value a, Value w) {
    Fun aaf = isFn(aa, '⍶');
    int am = ww.asInt();
    if (am < 0) {
      for (int i = 0; i < -am; i++) {
        w = aaf.call(a, w);
      }
    } else for (int i = 0; i < am; i++) {
      w = aaf.callInvW(a, w);
    }
    return w;
  }
  public Value callInvA(Value aa, Value ww, Value a, Value w) {
    Fun aaf = isFn(aa, '⍶');
    int am = ww.asInt();
    if (am== 1) return aaf.callInvA(a, w);
    if (am==-1) return aaf.callInvA(w, a);
    
    throw new DomainError("f⍣N: ⍺-inverting is only possible when N∊¯1 1", this, ww);
  }
  
  public Value under(Value aa, Value ww, Obj o, Value w, DerivedDop derv) {
    Fun aaf = isFn(aa, '⍶');
    int n = ww.asInt();
    return repeat(aaf, n, o, w);
  }
  public Value repeat(Fun aa, int n, Obj o, Value w) { // todo don't do recursion?
    if (n==0) {
      return o instanceof Fun? ((Fun) o).call(w) : (Value) o;
    }
    
    return repeat(aa, n-1, new Fun() { public String repr() { return aa.repr(); }
      public Value call(Value w) {
        return aa.under(o, w);
      }
    }, w);
  }
}