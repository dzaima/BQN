package APL.types.functions.builtins.dops;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.functions.*;

public class AfterBuiltin extends Dop {
  public String repr() {
    return "⟜";
  }
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    return call(f, g, x, x, derv);
  }
  
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    return f.call(w, g.call(x));
  }
  
  public Value callInv(Value f, Value g, Value x) {
    if (f.notIdentity()) throw new DomainError("𝔽⟜𝕘⁼: 𝕘 cannot be a function", this, f);
    return f.callInvA(x, g);
  }
  
  public Value under(Value f, Value g, Value o, Value x, DerivedDop derv) {
    if (f.notIdentity()) throw new DomainError("⌾(𝔽⟜𝕘): 𝕘 cannot be a function", this, f);
    return f.underA(o, x, g);
  }
}