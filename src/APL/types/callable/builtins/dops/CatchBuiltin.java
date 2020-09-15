package APL.types.callable.builtins.dops;

import APL.types.Value;
import APL.types.callable.DerivedDop;
import APL.types.callable.builtins.Md2Builtin;

public class CatchBuiltin extends Md2Builtin {
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