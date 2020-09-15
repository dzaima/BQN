package APL.types.callable.builtins.dops;

import APL.errors.SyntaxError;
import APL.types.*;
import APL.types.callable.DerivedDop;
import APL.types.callable.builtins.DopBuiltin;

public class JotBuiltin extends DopBuiltin {
  @Override public String repr() {
    return "∘";
  }
  
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    if (g instanceof Fun) {
      if (f instanceof Fun) {
        return f.call(g.call(x));
      } else {
        return g.call(f, x);
      }
    } else {
      if (f instanceof Fun) return f.call(x, g);
      throw new SyntaxError("arr∘arr makes no sense", this);
    }
  }
  public Value callInv(Value f, Value g, Value x) {
    if (g instanceof Fun) {
      if (f instanceof Fun) {
        return g.callInv(f.callInv(x));
      } else {
        return g.callInvX(f, x);
      }
    } else {
      if (f instanceof Fun) return f.callInvW(x, g);
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
    return f.call(w, g.call(x));
  }
  
  public Value callInvX(Value f, Value g, Value w, Value x) {
    return g.callInv(f.callInvX(w, x));
  }
  
  public Value callInvW(Value f, Value g, Value w, Value x) {
    return f.callInvW(w, g.call(x));
  }
  
  public Value under(Value f, Value g, Value o, Value x, DerivedDop derv) {
    if (g instanceof Fun) {
      if (f instanceof Fun) {
        return g.under(new Fun() { public String repr() { return f.repr(); }
          public Value call(Value x) {
            return f.under(o, x);
          }
        }, x);
      } else {
        return g.underW(o, f, x);
      }
    } else {
      if (f instanceof Fun) {
        return f.underA(o, x, g);
      } else {
        throw new SyntaxError("arr∘arr makes no sense", this);
      }
    }
  }
}