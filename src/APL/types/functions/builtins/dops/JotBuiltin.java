package APL.types.functions.builtins.dops;

import APL.errors.SyntaxError;
import APL.types.*;
import APL.types.functions.*;

public class JotBuiltin extends Dop {
  @Override public String repr() {
    return "∘";
  }
  
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    if (g instanceof Fun) {
      if (f instanceof Fun) {
        return ((Fun) f).call(((Fun) g).call(x));
      } else {
        return ((Fun) g).call(f, x);
      }
    } else {
      if (f instanceof Fun) return ((Fun) f).call(x, g);
      throw new SyntaxError("arr∘arr makes no sense", this);
    }
  }
  public Value callInv(Value f, Value g, Value x) {
    if (g instanceof Fun) {
      if (f instanceof Fun) {
        return ((Fun) g).callInv(((Fun) f).callInv(x));
      } else {
        return ((Fun) g).callInvW(f, x);
      }
    } else {
      if (f instanceof Fun) return ((Fun) f).callInvA(x, g);
      throw new SyntaxError("arr∘arr makes no sense", this);
    }
  }
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    if (!(f instanceof Fun)) {
      throw new SyntaxError("operands of dyadically applied ∘ must be functions, but 𝔽 is "+f.humanType(true), this, f);
    }
    if (!(g instanceof Fun)) {
      throw new SyntaxError("operands of dyadically applied ∘ must be functions, but 𝔾 is "+g.humanType(true), this, g);
    }
    return ((Fun) f).call(w, ((Fun) g).call(x));
  }
  
  public Value callInvW(Value f, Value g, Value w, Value x) {
    Fun aaf = f.asFun(); Fun wwf = g.asFun();
    return wwf.callInv(aaf.callInvW(w, x));
  }
  
  public Value callInvA(Value f, Value g, Value w, Value x) {
    Fun aaf = f.asFun(); Fun wwf = g.asFun();
    return aaf.callInvA(w, wwf.call(x));
  }
  
  public Value under(Value f, Value g, Value o, Value w, DerivedDop derv) {
    if (g instanceof Fun) {
      Fun wwf = (Fun) g;
      if (f instanceof Fun) {
        Fun gf = (Fun) f;
        return wwf.under(new Fun() { public String repr() { return gf.repr(); }
          public Value call(Value x) {
            return gf.under(o, x);
          }
        }, w);
      } else {
        return wwf.underW(o, f, w);
      }
    } else {
      if (f instanceof Fun) {
        return ((Fun) f).underA(o, w, g);
      } else {
        throw new SyntaxError("arr∘arr makes no sense", this);
      }
    }
  }
}