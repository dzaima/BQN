package APL.types.functions.builtins.dops;

import APL.types.Value;
import APL.types.functions.DerivedDop;
import APL.types.functions.builtins.DopBuiltin;

public class AtopBuiltin extends DopBuiltin {
  public String repr() {
    return "∘";
  }
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    return f.call(g.call(x));
  }
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    return f.call(g.call(w, x));
  }
  
  // +TODO inverses (from OldJotDiaeresisBuiltin)
}