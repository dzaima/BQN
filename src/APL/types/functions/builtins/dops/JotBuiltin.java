package APL.types.functions.builtins.dops;

import APL.errors.SyntaxError;
import APL.types.*;
import APL.types.functions.*;

public class JotBuiltin extends Dop {
  @Override public String repr() {
    return "∘";
  }
  
  
  public Value call(Value aa, Value ww, Value w, DerivedDop derv) {
    if (ww instanceof Fun) {
      if (aa instanceof Fun) {
        return ((Fun)aa).call(((Fun)ww).call(w));
      } else {
        return ((Fun)ww).call(aa, w);
      }
    } else {
      if (aa instanceof Fun) return ((Fun) aa).call(w, ww);
      throw new SyntaxError("arr∘arr makes no sense", this);
    }
  }
  public Value callInv(Value aa, Value ww, Value w) {
    if (ww instanceof Fun) {
      if (aa instanceof Fun) {
        return ((Fun)ww).callInv(((Fun)aa).callInv(w));
      } else {
        return ((Fun)ww).callInvW(aa, w);
      }
    } else {
      if (aa instanceof Fun) return ((Fun) aa).callInvA(w, ww);
      throw new SyntaxError("arr∘arr makes no sense", this);
    }
  }
  public Value call(Value aa, Value ww, Value a, Value w, DerivedDop derv) {
    if (!(aa instanceof Fun)) {
      throw new SyntaxError("operands of dyadically applied ∘ must be functions, but ⍶ is "+aa.humanType(true), this, aa);
    }
    if (!(ww instanceof Fun)) {
      throw new SyntaxError("operands of dyadically applied ∘ must be functions, but ⍹ is "+ww.humanType(true), this, ww);
    }
    return ((Fun)aa).call(a, ((Fun)ww).call(w));
  }
  
  public Value callInvW(Value aa, Value ww, Value a, Value w) {
    Fun aaf = isFn(aa, '⍶'); Fun wwf = isFn(ww, '⍹');
    return wwf.callInv(aaf.callInvW(a, w));
  }
  
  public Value callInvA(Value aa, Value ww, Value a, Value w) {
    Fun aaf = isFn(aa, '⍶'); Fun wwf = isFn(ww, '⍹');
    return aaf.callInvA(a, wwf.call(w));
  }
  
  public Value under(Value aa, Value ww, Value o, Value w, DerivedDop derv) {
    if (ww instanceof Fun) {
      Fun wwf = (Fun) ww;
      if (aa instanceof Fun) {
        Fun gf = (Fun) aa;
        return wwf.under(new Fun() { public String repr() { return gf.repr(); }
          public Value call(Value w) {
            return gf.under(o, w);
          }
        }, w);
      } else {
        return wwf.underW(o, aa, w);
      }
    } else {
      if (aa instanceof Fun) {
        return ((Fun) aa).underA(o, w, ww);
      } else {
        throw new SyntaxError("arr∘arr makes no sense", this);
      }
    }
  }
}