package APL.types.functions.builtins.dops;

import APL.errors.DomainError;
import APL.types.Value;
import APL.types.functions.*;

public class BeforeBuiltin extends Dop {
  public String repr() {
    return "⊸";
  }
  
  public Value call(Value aa, Value ww, Value w, DerivedDop derv) {
    return call(aa, ww, w, w, derv);
  }
  
  public Value call(Value aa, Value ww, Value a, Value w, DerivedDop derv) {
    return ww.asFun().call(aa.asFun().call(a), w);
  }
  
  public Value callInv(Value aa, Value ww, Value w) {
    if (aa.notIdentity()) throw new DomainError("𝕗⊸𝔾⁼: 𝕗 cannot be a function", this, aa);
    return ww.asFun().callInvW(aa, w);
  }
  
  public Value under(Value aa, Value ww, Value o, Value w, DerivedDop derv) {
    if (aa.notIdentity()) throw new DomainError("⌾(𝕗⊸𝔾): 𝕗 cannot be a function", this, aa);
    return ww.asFun().underW(o, aa, w);
  }
}
