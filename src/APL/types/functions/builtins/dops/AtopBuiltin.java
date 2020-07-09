package APL.types.functions.builtins.dops;

import APL.types.Value;
import APL.types.functions.*;

public class AtopBuiltin extends Dop {
  public String repr() {
    return "∘";
  }
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    return f.asFun().call(g.asFun().call(x));
  }
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    return f.asFun().call(g.asFun().call(w, x));
  }
  
  // +TODO inverses (from OldJotDiaeresisBuiltin)
}
