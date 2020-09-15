package APL.types.functions.builtins.dops;

import APL.types.Value;
import APL.types.functions.DerivedDop;
import APL.types.functions.builtins.DopBuiltin;

public class CatchBuiltin extends DopBuiltin {
  public String repr() {
    return "⎊";
  }
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    try {
      return f.call(x);
    } catch (Throwable e) {
      return g.call(x);
    }
  }
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    try {
      return f.call(w, x);
    } catch (Throwable e) {
      return g.call(w, x);
    }
  }
}