package APL.types.functions.builtins.dops;

import APL.errors.DomainError;
import APL.types.Value;
import APL.types.functions.*;

public class BeforeBuiltin extends Dop {
  public String repr() {
    return "⊸";
  }
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    return call(f, g, x, x, derv);
  }
  
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    return g.call(f.call(w), x);
  }
  
  public Value callInv(Value f, Value g, Value x) {
    if (f.notIdentity()) throw new DomainError("𝕗⊸𝔾⁼: 𝕗 cannot be a function", this, f);
    return g.callInvW(f, x);
  }
  
  public Value under(Value f, Value g, Value o, Value x, DerivedDop derv) {
    if (f.notIdentity()) throw new DomainError("⌾(𝕗⊸𝔾): 𝕗 cannot be a function", this, f);
    return g.underW(o, f, x);
  }
}